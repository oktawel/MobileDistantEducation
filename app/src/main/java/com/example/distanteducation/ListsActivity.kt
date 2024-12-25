package com.example.distanteducation

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.example.distanteducation.functions.LogoutHelper
import com.example.distanteducation.serverConection.Group
import com.example.distanteducation.serverConection.Lecturer
import com.example.distanteducation.serverConection.RetrofitClient
import com.example.distanteducation.serverConection.Student
import com.example.distanteducation.serverConection.UserSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ListsActivity : AppCompatActivity() {
    private lateinit var btnAdd: Button
    private lateinit var viewTittle: TextView
    private lateinit var emptyMessage: TextView
    private lateinit var type: String
    private lateinit var container: LinearLayout
    private lateinit var token: String

    private var lecturerSearch = listOf("Имя", "Фамилия")
    private var studentSearch = listOf("Имя", "Фамилия","Группа")
    private var groupSearch = listOf("Название")

    private var groupsList: MutableList<Group> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lists)

        btnAdd = findViewById(R.id.btnAdd)
        viewTittle = findViewById(R.id.tvTitle)
        emptyMessage = findViewById(R.id.emptyMessage)
        token = UserSession.token ?: return
        type = intent.getStringExtra("type") ?: return

        val userName: TextView = findViewById(R.id.user_name)

        userName.text = UserSession.user!!.name

        val btnLogout: ImageView = findViewById(R.id.btn_exit_acc)
        btnLogout.setOnClickListener {
            LogoutHelper.performLogout(this)
        }
        val btnBack: ImageButton = findViewById(R.id.btn_back)
        btnBack.setOnClickListener {
            finish()
        }
        val showDialogButton: ImageButton = findViewById(R.id.search_btn)
        showDialogButton.setOnClickListener {
            showModalDialog()
        }

        loadData()
    }

    // Метод для отображения модального окна
    private fun showModalDialog() {
        val dialogView = layoutInflater.inflate(R.layout.search_window, null)

        val spinnerCriteria: Spinner = dialogView.findViewById(R.id.spinnerSearchCriteria)
        val spinnerGroup: Spinner = dialogView.findViewById(R.id.spinnerGroup)
        val editText: EditText = dialogView.findViewById(R.id.editText)

        var criteriaAdapter: ArrayAdapter<String>? = null

        when (type) {
            "lecturer" -> {
                criteriaAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, lecturerSearch)
                criteriaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerCriteria.adapter = criteriaAdapter
            }
            "student" -> {
                criteriaAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, studentSearch)
                criteriaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerCriteria.adapter = criteriaAdapter

                if (groupsList.isEmpty()){
                    // Загружаем группы для второго выпадающего списка (если тип "student")
                    CoroutineScope(Dispatchers.IO).launch {
                            val response = RetrofitClient.apiService.getGroups("Bearer $token")
                            if (response.isSuccessful) {
                                val groups = response.body() ?: emptyList()
                                val group = Group(id = 0, name = "Выберите группу")
                                val mutableGroups = groups.toMutableList()
                                mutableGroups.add(0, group)
                                groupsList = mutableGroups
                            } else {
                                Log.e("showModalDialog", "Failed to load groups: ${response.code()}")
                            }
                    }
                }
                val groupNames = groupsList.map { it.name }

                val groupAdapter = ArrayAdapter(
                    this@ListsActivity,
                    android.R.layout.simple_spinner_item,
                    groupNames
                )
                groupAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerGroup.adapter = groupAdapter
            }
            "group" -> {
                criteriaAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, groupSearch)
                criteriaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerCriteria.adapter = criteriaAdapter
            }
        }

        // Логика отображения/скрытия EditText в зависимости от критерия поиска
        spinnerCriteria.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedCriterion = parent?.getItemAtPosition(position).toString()

                when (selectedCriterion) {
                    "Название" -> {
                        editText.visibility =  View.VISIBLE
                        editText.hint = "Название"
                        spinnerGroup.visibility =  View.GONE
                    }
                    "Имя" -> {
                        editText.visibility =  View.VISIBLE
                        editText.hint = "Имя"
                        spinnerGroup.visibility =  View.GONE
                    }
                    "Фамилия" -> {
                        editText.visibility =  View.VISIBLE
                        editText.hint = "Фамилия"
                        spinnerGroup.visibility =  View.GONE
                    }
                    "Группа" -> {
                        editText.visibility =  View.GONE
                        spinnerGroup.visibility =  View.VISIBLE
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Создание и отображение диалога
        val builder = AlertDialog.Builder(this)
            .setTitle("Поиск")
            .setView(dialogView)
            .setNeutralButton("Очистить поиск") { dialog, _ ->
                container.removeAllViews()
                when (type) {
                    "lecturer" -> loadLecturers()
                    "student" -> loadStudents()
                    "group" ->  loadGroups()
                }
                dialog.dismiss()
            }
            .setPositiveButton("OK") { dialog, _ ->
                val selectedCriterion = spinnerCriteria.selectedItem.toString()
                val inputText = editText.text.toString()
                val selectedGroup = spinnerGroup.selectedItem?.toString()
                container.removeAllViews()
                when (selectedCriterion) {
                    "Название" -> searchByName(inputText)
                    "Имя" -> searchByName(inputText)
                    "Фамилия" -> searchBySurname(inputText)
                    "Группа" -> {
                        searchByGroup(groupsList.find { it.name == selectedGroup }?.id!!)
                    }
                    else -> Toast.makeText(this, "Некорректный критерий поиска", Toast.LENGTH_SHORT).show()
                }

                dialog.dismiss()
            }
            .setNegativeButton("Отмена") { dialog, _ -> dialog.dismiss() }

        builder.create().show()
    }

    private fun searchByName(query: String) {
        container.removeAllViews()
        when (type) {
            "lecturer" -> {
                if (query == ""){
                    loadLecturers()
                }
                else{
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val response = RetrofitClient.apiService.getAllLecturersByName(
                                token ="Bearer $token",
                                name = query
                            )
                            if (response.isSuccessful) {
                                val lecturers = response.body() ?: emptyList()
                                container.removeAllViews()
                                if (lecturers.isEmpty()){
                                    emptyMessage.visibility = View.VISIBLE
                                    emptyMessage.text = "Лекторов с таким именем не найдено"
                                }
                                else{
                                    withContext(Dispatchers.Main) {
                                        for (lecturer in lecturers) {
                                            addListLecturersView(lecturer)
                                        }
                                    }
                                }
                            } else {
                                withContext(Dispatchers.Main) {
                                    Log.d("Search", response.toString())
                                }
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                Log.d("Search", e.message.toString())
                            }
                        }
                    }
                }
            }
            "student" -> {
                if (query == ""){
                    loadStudents()
                }
                else{
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val response = RetrofitClient.apiService.getAllStudentsByName(
                                token ="Bearer $token",
                                name = query
                            )
                            if (response.isSuccessful) {
                                val students = response.body() ?: emptyList()
                                container.removeAllViews()
                                if (students.isEmpty()){
                                    emptyMessage.visibility = View.VISIBLE
                                    emptyMessage.text = "Студентов с таким именем не найдено"
                                }
                                else{
                                    withContext(Dispatchers.Main) {
                                        for (student in students) {
                                            addListStudentsView(student)
                                        }
                                    }
                                }
                            } else {
                                withContext(Dispatchers.Main) {
                                    Log.d("Search", response.toString())
                                }
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                Log.d("Search", e.message.toString())
                            }
                        }
                    }
                }
            }
            "group" -> {
                if (query == ""){
                    loadGroups()
                }
                else{
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val response = RetrofitClient.apiService.getAllGroupsByName(
                                token ="Bearer $token",
                                name = query
                            )
                            if (response.isSuccessful) {
                                val groups = response.body() ?: emptyList()
                                container.removeAllViews()
                                if (groups.isEmpty()){
                                    emptyMessage.visibility = View.VISIBLE
                                    emptyMessage.text = "Групп с таким названием не найдено"
                                }
                                else{
                                    withContext(Dispatchers.Main) {
                                        for (group in groups) {
                                            addListGroupsView(group)
                                        }
                                    }
                                }
                            } else {
                                withContext(Dispatchers.Main) {
                                    Log.d("Search", response.toString())
                                }
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                Log.d("Search", e.message.toString())
                            }
                        }
                    }
                }
            }
        }
    }

    private fun searchBySurname(query: String) {
        container.removeAllViews()
        when (type) {
            "lecturer" -> {
                if (query == ""){
                    loadLecturers()
                }
                else{
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val response = RetrofitClient.apiService.getAllLecturersBySurname(
                                token ="Bearer $token",
                                surname = query
                            )
                            if (response.isSuccessful) {
                                val lecturers = response.body() ?: emptyList()
                                container.removeAllViews()
                                if (lecturers.isEmpty()){
                                    emptyMessage.visibility = View.VISIBLE
                                    emptyMessage.text = "Лекторов с такой фамилией не найдено"
                                }
                                else{
                                    withContext(Dispatchers.Main) {

                                        for (lecturer in lecturers) {
                                            addListLecturersView(lecturer)
                                        }
                                    }
                                }

                            } else {
                                withContext(Dispatchers.Main) {
                                    Log.d("Search", response.toString())
                                }
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                Log.d("Search", e.message.toString())
                            }
                        }
                    }
                }
            }
            "student" -> {
                if (query == ""){
                    loadStudents()
                }
                else{
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val response = RetrofitClient.apiService.getAllStudentsBySurname(
                                token ="Bearer $token",
                                surname = query
                            )
                            if (response.isSuccessful) {
                                val students = response.body() ?: emptyList()
                                container.removeAllViews()
                                if (students.isEmpty()){
                                    emptyMessage.visibility = View.VISIBLE
                                    emptyMessage.text = "Студентов с такой фамилией не найдено"
                                }
                                else{
                                    withContext(Dispatchers.Main) {
                                        for (student in students) {
                                            addListStudentsView(student)
                                        }
                                    }
                                }

                            } else {
                                withContext(Dispatchers.Main) {
                                    Log.d("Search", response.toString())
                                }
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                Log.d("Search", e.message.toString())
                            }
                        }
                    }
                }
            }
        }
    }

    private fun searchByGroup(groupId: Long) {
        container.removeAllViews()
        if (groupId == 0.toLong()){
            loadStudents()
        }
        else{
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = RetrofitClient.apiService.getAllStudentsByGroup(
                        token ="Bearer $token",
                        id = groupId
                    )
                    if (response.isSuccessful) {
                        val students = response.body() ?: emptyList()
                        container.removeAllViews()
                        if (students.isEmpty()){
                            emptyMessage.visibility = View.VISIBLE
                            emptyMessage.text = "Студентов с такой группой не найдено"
                        }
                        else{
                            withContext(Dispatchers.Main) {
                                for (student in students) {
                                    addListStudentsView(student)
                                }
                            }
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            Log.d("Search", response.toString())
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Log.d("Search", e.message.toString())
                    }
                }
            }
        }
    }


    override fun onResume() {
        super.onResume()
        loadData()
    }


    private fun loadData() {
        container = findViewById<LinearLayout>(R.id.containerList)

        when (type) {
            "lecturer" -> loadLecturers()
            "student" -> loadStudents()
            "group" -> loadGroups()
        }
    }

    private fun startAdd(){
        val intent = Intent(this, AddActivity::class.java)
        intent.putExtra("type", type)
        startActivity(intent)
    }

    private fun startEditLecturer(lecturer: Lecturer){
        val intent = Intent(this, EditActivity::class.java)
        intent.putExtra("type", type)
        intent.putExtra("Lecturer", lecturer)
        startActivity(intent)
    }

    private fun startEditGroup(group: Group){
        val intent = Intent(this, EditActivity::class.java)
        intent.putExtra("type", type)
        intent.putExtra("Group", group)

        startActivity(intent)
    }

    private fun startEditStudent(student: Student){
        val intent = Intent(this, EditActivity::class.java)
        intent.putExtra("type", type)
        intent.putExtra("Student", student)
        startActivity(intent)
    }

    private fun loadLecturers() {
        viewTittle.text = "Справочник лекторов"
        btnAdd.text = "Добавить лектора"
        btnAdd.setOnClickListener {
            startAdd()
        }
        emptyMessage.visibility = View.GONE
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.apiService.getLecturers("Bearer $token")
                if (response.isSuccessful) {
                    val lecturers = response.body() ?: emptyList()

                    withContext(Dispatchers.Main) {
                        container.removeAllViews()
                        for (lecturer in lecturers) {
                            addListLecturersView(lecturer)
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@ListsActivity,
                            "Ошибка загрузки данных: ${response.code()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@ListsActivity,
                        "Ошибка соединения: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun loadStudents() {
        viewTittle.text = "Справочник студентов"
        btnAdd.text = "Добавить студента"
        btnAdd.setOnClickListener {
            startAdd()
        }
        emptyMessage.visibility = View.GONE
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.apiService.getStudents("Bearer $token")
                if (response.isSuccessful) {
                    val students = response.body() ?: emptyList()

                    withContext(Dispatchers.Main) {
                        container.removeAllViews()
                        for (student in students) {
                            addListStudentsView(student)
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@ListsActivity,
                            "Ошибка загрузки данных: ${response.code()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@ListsActivity,
                        "Ошибка соединения: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun loadGroups() {
        viewTittle.text = "Справочник групп"
        btnAdd.text = "Добавить группу"
        btnAdd.setOnClickListener {
            startAdd()
        }
        emptyMessage.visibility = View.GONE
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.apiService.getGroups("Bearer $token")
                if (response.isSuccessful) {
                    val groups = response.body() ?: emptyList()

                    withContext(Dispatchers.Main) {
                        container.removeAllViews()
                        for (group in groups) {
                            addListGroupsView(group)
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@ListsActivity,
                            "Ошибка загрузки данных: ${response.code()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@ListsActivity,
                        "Ошибка соединения: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }


    private fun addListLecturersView(lecturer: Lecturer) {

        val lecturerLayout = createLayoutLecturer(lecturer)

        val textView = createTextField(lecturer.id, lecturer.name, lecturer.surname)
        val editButton = createBtnEditLecturer(lecturer)
        val deleteButton = createBtnDelete(lecturer.id)

        lecturerLayout.addView(textView)
        lecturerLayout.addView(editButton)
        lecturerLayout.addView(deleteButton)

        container.addView(lecturerLayout)
    }

    private fun addListStudentsView(student: Student) {

        val lecturerLayout = createLayoutStudent(student)

        val textView = createTextField(student.id, student.name, student.surname)
        val editButton = createBtnEditStudent(student)
        val deleteButton = createBtnDelete(student.id)

        lecturerLayout.addView(textView)
        lecturerLayout.addView(editButton)
        lecturerLayout.addView(deleteButton)

        container.addView(lecturerLayout)
    }

    private fun addListGroupsView(group: Group) {

        val lecturerLayout = createLayoutGroup(group)

        val textView = createTextField(group.id, group.name, "")
        val editButton = createBtnEditGroup(group)
        val deleteButton = createBtnDelete(group.id)

        lecturerLayout.addView(textView)
        lecturerLayout.addView(editButton)
        lecturerLayout.addView(deleteButton)

        container.addView(lecturerLayout)
    }



    private fun createLayoutStudent(student: Student): LinearLayout {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(8, 8, 0, 8)
            }
            setPadding(16, 16, 16, 16)
        }
        layout.setOnClickListener{
            val intent = Intent(this, InfoActivity::class.java)
            intent.putExtra("type", type)
            intent.putExtra("Student", student)
            startActivity(intent)
        }
        return layout
    }

    private fun createLayoutLecturer(lecturer: Lecturer): LinearLayout {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(8, 8, 0, 8)
            }
            setPadding(16, 16, 16, 16)
        }
        layout.setOnClickListener{
            val intent = Intent(this, InfoActivity::class.java)
            intent.putExtra("type", type)
            intent.putExtra("Lecturer", lecturer)
            startActivity(intent)
        }
        return layout
    }

    private fun createLayoutGroup(group: Group): LinearLayout {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(8, 8, 0, 8)
            }
            setPadding(16, 16, 16, 16)
        }
        layout.setOnClickListener{
            val intent = Intent(this, InfoActivity::class.java)
            intent.putExtra("type", type)
            intent.putExtra("Group", group)
            startActivity(intent)
        }
        return layout
    }

    private fun createTextField(id:Long, name:String, surname:String,): TextView {
        val textView = TextView(this).apply {
            text = "${name} ${surname}"
            textSize = 20f
            setTextColor(ContextCompat.getColor(context, android.R.color.black))
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2f).apply { setMargins(0, 35, 0, 8) }
        }
        return textView
    }

    private fun createBtnEditLecturer(lecturer: Lecturer): CardView {
        val editButton = CardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(100, 100).apply {
                setMargins(16, 16, 16, 16)
            }
            radius = 75f
            cardElevation = 10f
            setCardBackgroundColor(ContextCompat.getColor(context, R.color.field_text))
            addView(ImageView(context).apply {
                setImageResource(R.drawable.baseline_edit_24)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
                )
                scaleType = ImageView.ScaleType.CENTER_INSIDE
                setColorFilter(ContextCompat.getColor(context, R.color.button))
            })

            setOnClickListener {
                startEditLecturer(lecturer)
            }
        }
        return editButton
    }

    private fun createBtnEditGroup(group: Group): CardView {
        val editButton = CardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(100, 100).apply {
                setMargins(16, 16, 16, 16)
            }
            radius = 75f
            cardElevation = 10f
            setCardBackgroundColor(ContextCompat.getColor(context, R.color.field_text))
            addView(ImageView(context).apply {
                setImageResource(R.drawable.baseline_edit_24)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
                )
                scaleType = ImageView.ScaleType.CENTER_INSIDE
                setColorFilter(ContextCompat.getColor(context, R.color.button))
            })

            setOnClickListener {
                startEditGroup(group)
            }
        }
        return editButton
    }

    private fun createBtnEditStudent(student: Student): CardView {
        val editButton = CardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(100, 100).apply {
                setMargins(16, 16, 16, 16)
            }
            radius = 75f
            cardElevation = 10f
            setCardBackgroundColor(ContextCompat.getColor(context, R.color.field_text))
            addView(ImageView(context).apply {
                setImageResource(R.drawable.baseline_edit_24)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
                )
                scaleType = ImageView.ScaleType.CENTER_INSIDE
                setColorFilter(ContextCompat.getColor(context, R.color.button))
            })

            setOnClickListener {
                startEditStudent(student)
            }
        }
        return editButton
    }

    private fun createBtnDelete(id:Long): CardView {
        val deleteButton = CardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(100, 100).apply {
                setMargins(16, 16, 16, 16)
            }
            radius = 75f
            cardElevation = 10f
            setCardBackgroundColor(ContextCompat.getColor(context, R.color.redDelete))
            addView(ImageView(context).apply {
                setImageResource(R.drawable.baseline_delete_24)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
                )
                scaleType = ImageView.ScaleType.CENTER_INSIDE
                setColorFilter(ContextCompat.getColor(context, R.color.button))
            })
            setOnClickListener {
                when (type) {
                    "lecturer" -> deleteLecturer(id)
                    "student" -> deleteStudent(id)
                    "group" -> deleteGroup(id)
                }
            }
        }
        return deleteButton
    }



    private fun deleteStudent(studentId: Long) {
        AlertDialog.Builder(this)
            .setTitle("Удаление студента")
            .setMessage("Вы уверены, что хотите удалить студента?")
            .setPositiveButton("Да") { _, _ ->
                val apiService = RetrofitClient.apiService

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val response = apiService.deleteStudent(
                            token = "Bearer ${UserSession.token}",
                            studentId = studentId
                        )
                        withContext(Dispatchers.Main) {
                            if (response.isSuccessful) {
                                Toast.makeText(
                                    this@ListsActivity,
                                    "Студент успешно удалён",
                                    Toast.LENGTH_SHORT
                                ).show()
                                loadStudents()
                            } else {
                                Toast.makeText(
                                    this@ListsActivity,
                                    "Ошибка удаления студента",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@ListsActivity,
                                "Ошибка: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun deleteLecturer(lecturerId: Long) {
        AlertDialog.Builder(this)
            .setTitle("Удаление лектора")
            .setMessage("Вы уверены, что хотите удалить лектора?")
            .setPositiveButton("Да") { _, _ ->
                val apiService = RetrofitClient.apiService

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val response = apiService.deleteLecturer(
                            token = "Bearer ${UserSession.token}",
                            lecturerId = lecturerId
                        )
                        withContext(Dispatchers.Main) {
                            if (response.isSuccessful) {
                                Toast.makeText(
                                    this@ListsActivity,
                                    "Лектор успешно удалён",
                                    Toast.LENGTH_SHORT
                                ).show()
                                loadLecturers()
                            } else {
                                Toast.makeText(
                                    this@ListsActivity,
                                    "Ошибка удаления лектора",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@ListsActivity,
                                "Ошибка: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun deleteGroup(groupId: Long) {
        AlertDialog.Builder(this)
            .setTitle("Удаление группы")
            .setMessage("Вы уверены, что хотите удалить группу?")
            .setPositiveButton("Да") { _, _ ->
                val apiService = RetrofitClient.apiService

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val response = apiService.deleteGroup(
                            token = "Bearer ${UserSession.token}",
                            groupId = groupId
                        )
                        withContext(Dispatchers.Main) {
                            if (response.isSuccessful) {
                                Toast.makeText(
                                    this@ListsActivity,
                                    "Группа успешно удалёна",
                                    Toast.LENGTH_SHORT
                                ).show()
                                loadGroups()
                            } else {
                                Toast.makeText(
                                    this@ListsActivity,
                                    "Ошибка удаления группы",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@ListsActivity,
                                "Ошибка: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }


}


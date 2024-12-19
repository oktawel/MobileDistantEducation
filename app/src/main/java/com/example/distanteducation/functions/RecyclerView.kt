package com.example.distanteducation.functions

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.distanteducation.R
import com.example.distanteducation.serverConection.OptionAnswer
import com.example.distanteducation.serverConection.QuestionExecute

class QuestionsAdapter(
    private val questions: List<QuestionExecute>,
    private val onAnswerSelected: (Long, OptionAnswer) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val TYPE_FREE_RESPONSE = 1
        const val TYPE_SINGLE_CHOICE = 2
        const val TYPE_MULTIPLE_CHOICE = 3
        const val TYPE_TRUE_FALSE = 4
    }

    override fun getItemViewType(position: Int): Int {
        return questions[position].typeQuestionID.toInt()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_FREE_RESPONSE -> FreeResponseViewHolder(
                inflater.inflate(R.layout.item_free_response, parent, false)
            )
            TYPE_SINGLE_CHOICE -> SingleChoiceViewHolder(
                inflater.inflate(R.layout.item_single_choice, parent, false)
            )
            TYPE_MULTIPLE_CHOICE -> MultipleChoiceViewHolder(
                inflater.inflate(R.layout.item_multiple_choice, parent, false)
            )
            TYPE_TRUE_FALSE -> TrueFalseViewHolder(
                inflater.inflate(R.layout.item_true_false, parent, false)
            )
            else -> throw IllegalArgumentException("Unknown question type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val question = questions[position]
        when (holder) {
            is FreeResponseViewHolder -> holder.bind(question, onAnswerSelected)
            is SingleChoiceViewHolder -> holder.bind(question, onAnswerSelected)
            is MultipleChoiceViewHolder -> holder.bind(question, onAnswerSelected)
            is TrueFalseViewHolder -> holder.bind(question, onAnswerSelected)
        }
    }

    override fun getItemCount(): Int = questions.size

    // ViewHolder для свободного ответа
    class FreeResponseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val questionText: TextView = itemView.findViewById(R.id.questionText)
        private val answerInput: EditText = itemView.findViewById(R.id.answerInput)

        fun bind(question: QuestionExecute, onAnswerSelected: (Long, OptionAnswer) -> Unit) {
            questionText.text = question.text
            answerInput.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
                    val answer = answerInput.text.toString()
                    onAnswerSelected(question.id, OptionAnswer(optionId = null, textAnswer = answer))
                }

                override fun afterTextChanged(editable: Editable?) {
                }
            })
        }
    }


    // ViewHolder для вопроса с одним выбором
    class SingleChoiceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val questionText: TextView = itemView.findViewById(R.id.questionText)
        private val radioGroup: RadioGroup = itemView.findViewById(R.id.radioGroup)

        fun bind(question: QuestionExecute, onAnswerSelected: (Long, OptionAnswer) -> Unit) {
            questionText.text = question.text
            radioGroup.removeAllViews()
            radioGroup.setBackgroundResource(R.drawable.rounded_field2)
            question.options?.forEach { option ->
                val radioButton = RadioButton(itemView.context).apply {
                    text = option.text
                    id = option.id.toInt()
                }
                radioGroup.addView(radioButton)
            }
            radioGroup.setOnCheckedChangeListener { _, checkedId ->
                onAnswerSelected(question.id, OptionAnswer(optionId = checkedId.toLong(), textAnswer = null))
            }
        }
    }

    // ViewHolder для вопроса с несколькими выборами
    class MultipleChoiceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val questionText: TextView = itemView.findViewById(R.id.questionText)
        private val checkBoxGroup: LinearLayout = itemView.findViewById(R.id.checkBoxGroup)

        fun bind(question: QuestionExecute, onAnswerSelected: (Long, OptionAnswer) -> Unit) {
            questionText.text = question.text
            checkBoxGroup.removeAllViews()
            checkBoxGroup.setBackgroundResource(R.drawable.rounded_field2)

            question.options?.forEach { option ->
                val checkBox = CheckBox(itemView.context).apply {
                    text = option.text
                    id = option.id.toInt()
                    isChecked = false
                }
                checkBox.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        onAnswerSelected(
                            question.id,
                            OptionAnswer(optionId = option.id, textAnswer = null)
                        )
                    } else {
                        onAnswerSelected(
                            question.id,
                            OptionAnswer(optionId = option.id, textAnswer = null)
                        )
                    }
                }
                checkBoxGroup.addView(checkBox)
            }
        }
    }


    // ViewHolder для вопроса true/false
    class TrueFalseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val questionText: TextView = itemView.findViewById(R.id.questionText)
        private val radioGroup: RadioGroup = itemView.findViewById(R.id.radioGroupTrueFalse)

        fun bind(question: QuestionExecute, onAnswerSelected: (Long, OptionAnswer) -> Unit) {
            questionText.text = question.text
            radioGroup.setOnCheckedChangeListener { _, checkedId ->
                val isTrue = checkedId == R.id.radioTrue
                val optionId = question.options?.find { it.correct == isTrue }?.id
                onAnswerSelected(question.id, OptionAnswer(optionId = optionId, textAnswer = null))
            }
        }
    }
}

package com.example.notemake.Displays.HomeDisplay

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.graphics.Color
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.notemake.Displays.Helper.helperUtil
import com.example.notemake.R
import com.example.notemake.databinding.FragmentFirstBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private val sharedPref by lazy {
        activity?.getPreferences(Context.MODE_PRIVATE) ?: throw IllegalStateException("Activity cannot be null")
    }

    private var hardMode = false;
    private var wordSwap = false;

    private var currentData = mapOf<String, List<List<String>>>()
    private var foreignWords = mutableListOf<String>()

    private var totalGrouped: Int = 5
    private var runningGroupedScore: Int = 0
    private var chosenForeignWords = mutableListOf<String>()

    private lateinit var mainWord: TextView
    private lateinit var button1: Button
    private lateinit var button2: Button
    private lateinit var button3: Button
    private lateinit var button4: Button
    private lateinit var button5: Button

    private var topWord = ""

    private lateinit var rightButton: Button
    private lateinit var wrongButton: Button

    private lateinit var collection_button: Button
    private lateinit var difficulty_button: Button
    private lateinit var refresh_button: Button
    private lateinit var swapper_button: Button
    private lateinit var layout_button_options: LinearLayout
    private lateinit var layout_text_option: LinearLayout
    private lateinit var right_wrong_layout: LinearLayout

    private lateinit var manual_text_box: EditText



    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewWordsButton.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }

        mainWord = view.findViewById(R.id.main_word)
        button1 = view.findViewById(R.id.button_main_1)
        button2 = view.findViewById(R.id.button_main_2)
        button3 = view.findViewById(R.id.button_main_3)
        button4 = view.findViewById(R.id.button_main_4)
        button5 = view.findViewById(R.id.button_main_5)

        rightButton = view.findViewById(R.id.rightButton)
        wrongButton = view.findViewById(R.id.wrongButton)

        collection_button = view.findViewById(R.id.collection_size_button)
        difficulty_button = view.findViewById(R.id.difficulty_button)
        refresh_button = view.findViewById(R.id.refresh_button)
        swapper_button = view.findViewById(R.id.swapper)
        layout_button_options = view.findViewById(R.id.buttonLayouts)
        layout_text_option = view.findViewById(R.id.textLayout)

        manual_text_box = view.findViewById(R.id.manuallyWriteDifficultyTextBox)

        right_wrong_layout = view.findViewById(R.id.rightWrongLayout)

        collection_button.text = totalGrouped.toString()

        // Load words from SharedPreferences
        currentData = helperUtil.loadData(requireContext())!!

        chosenForeignWords = currentData.keys.toList().shuffled().take(totalGrouped).toMutableList()

        setButtonListeners(view)

        executeNextButton()

    }

    private fun setButtonListeners(view: View) {
        // Code to execute when buttonX is clicked
        button1.setOnClickListener {
            createRandomWords(view, topWord, 0)
        }
        button2.setOnClickListener {
            createRandomWords(view, topWord, 1)
        }
        button3.setOnClickListener {
            createRandomWords(view, topWord, 2)
        }
        button4.setOnClickListener {
            createRandomWords(view, topWord, 3)
        }
        button5.setOnClickListener {
            createRandomWords(view, topWord, 4)
        }
        collection_button.setOnClickListener {
            changeCollectionSize(view)
        }
        difficulty_button.setOnClickListener {
            changeDifficulty(view)
        }
        refresh_button.setOnClickListener {
            refreshWords()
        }
        swapper_button.setOnClickListener {
            swap()
        }

        // Set up listener for the submit button on the keyboard
        manual_text_box.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)) {
                // Handle the action here
                handleManualTextSubmit()
                true
            } else {
                false
            }
        })

        wrongButton.setOnClickListener {
            wrongButtonClicked()
        }

        rightButton.setOnClickListener {
            rightButtonClicked()
        }
    }

    private fun swap() {
        wordSwap = !wordSwap
    }
    private fun refreshWords() {

        chosenForeignWords = if (totalGrouped == -1) {
            currentData.keys.toList().shuffled().toMutableList()
        } else {
            currentData.keys.toList().shuffled().take(totalGrouped).toMutableList()
        }
        runningGroupedScore = 0

        executeNextButton()
    }

    private fun changeDifficulty(view: View) {
        hardMode = !hardMode
        if (hardMode) {
            layout_button_options.visibility = View.GONE
            layout_button_options.isEnabled = false

            layout_text_option.visibility = View.VISIBLE
            layout_text_option.isEnabled = true

            // Show keyboard
            manual_text_box.requestFocus()
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(manual_text_box, InputMethodManager.SHOW_IMPLICIT)
        }
        else {
            layout_button_options.visibility = View.VISIBLE
            layout_button_options.isEnabled = true

            layout_text_option.visibility = View.GONE
            layout_text_option.isEnabled = false

            // Hide keyboard
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(manual_text_box.windowToken, 0)
        }
    }

    private fun changeCollectionSize(view: View) {
        if (totalGrouped == 3) {
            totalGrouped = 5
            collection_button.text = totalGrouped.toString()
        }
        else if (totalGrouped == 5) {
            totalGrouped = 10
            collection_button.text = totalGrouped.toString()
        }
        else if (totalGrouped == 10) {
            totalGrouped = -1
            collection_button.text = "∞"
        }
        else {
            totalGrouped = 3
            collection_button.text = totalGrouped.toString()
        }
    }

    private fun handleManualTextSubmit() {
        //check if it's close to the right button, if so then make yellow or green, and call createRandomWords
        //and indicate with boolean they were correct
        var correctAnswer = chosenForeignWords[runningGroupedScore]
        var userAnswer = manual_text_box.text.toString()
        if (correctAnswer.lowercase().trim() == userAnswer.lowercase().trim()) {
            rightButtonClicked()
        }
        else {
            //Make text have correct answer, make color yellow
            manual_text_box.setBackgroundResource(R.drawable.rounded_edittext_yellow)
            manual_text_box.setTextColor(Color.BLACK)
            manual_text_box.setText(userAnswer.trim() + " | " + correctAnswer.trim())
            manual_text_box.isEnabled = false

            // Hide keyboard
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(manual_text_box.windowToken, 0)

            right_wrong_layout.visibility = View.VISIBLE




            //show Correct and Incorrect buttons
        }
    }

    private fun wrongButtonClicked() {
        val correctWordForeign = chosenForeignWords[runningGroupedScore]
        helperUtil.addStat(requireContext(), correctWordForeign, 1)
        runningGroupedScore = 0

        var count = 0
        //shuffle the groupedCollection, don't let the prev word be the next upcoming word
        chosenForeignWords = chosenForeignWords.shuffled().toMutableList()
        while (currentData[chosenForeignWords[0]]!![0].contains(topWord) && count++ != 5 && totalGrouped != -1) {
            chosenForeignWords = chosenForeignWords.shuffled().toMutableList()
        }

        executeNextButton()
    }

    private fun rightButtonClicked() {
        val correctWordForeign = chosenForeignWords[runningGroupedScore]
        helperUtil.addStat(requireContext(), correctWordForeign, 1)
        runningGroupedScore++

        if (runningGroupedScore == chosenForeignWords.size) {
            refreshWords()
        } else {
            executeNextButton()
        }
    }

    //sets the colors of the buttons if wrong, and then calls executeNextButton method
    //also either adds to runningScore or resets score
    private fun createRandomWords(view: View, correctWord: String, index: Int) {

        val buttonList = listOf(button1, button2, button3, button4, button5)

        //WRONG ANSWER
        if(correctWord != "" && !currentData[foreignWords[index]]!![0].contains(correctWord)) {
            val correctWordForeign = chosenForeignWords[runningGroupedScore]
            helperUtil.addStat(requireContext(), correctWordForeign, -1)
            runningGroupedScore = 0
            var count = 0
            //shuffle the groupedCollection, don't let the prev word be the next upcoming word
            chosenForeignWords = chosenForeignWords.shuffled().toMutableList()
            while (currentData[chosenForeignWords[0]]!![0].contains(correctWord) && count++ != 5 && totalGrouped != -1) {
                chosenForeignWords = chosenForeignWords.shuffled().toMutableList()
            }
            // Change colors
            var counter = 0
            buttonList.forEach {
                if (currentData[foreignWords[counter]]!![0].contains(correctWord))
                    it.setBackgroundColor(Color.GREEN)
                else
                    it.setBackgroundColor(Color.RED)
                counter++
            }

            // Disable buttons
            buttonList.forEach { it.isEnabled = false }

            // Pause execution in this coroutine scope for 3 seconds
            lifecycleScope.launch {
                delay(3000L)

                // Reset colors
                buttonList.forEach { it.setBackgroundColor(ContextCompat.getColor(requireContext(),
                    R.color.primary
                )) }

                // Enable buttons
                buttonList.forEach { it.isEnabled = true }
                executeNextButton()
            }
        }
        //CORRECT ANSWER
        else {
            val correctWordForeign = chosenForeignWords[runningGroupedScore]
            helperUtil.addStat(requireContext(), correctWordForeign, 1)
            runningGroupedScore++

            //if user got the whole collection correct in one go, give them a new list
            if (runningGroupedScore == chosenForeignWords.size) {
                chosenForeignWords = if (totalGrouped == -1) {
                    currentData.keys.toList().shuffled().toMutableList()
                } else {
                    currentData.keys.toList().shuffled().take(totalGrouped).toMutableList()
                }
                runningGroupedScore = 0

                //make all buttons green for 2 seconds to indicate user passed a collection
                buttonList.forEach {
                        it.setBackgroundColor(Color.GREEN)
                        it.isEnabled = false
                }

                lifecycleScope.launch {
                    delay(2000L)

                    // Reset colors
                    buttonList.forEach { it.setBackgroundColor(ContextCompat.getColor(requireContext(),
                        R.color.primary
                    )) }

                    // Enable buttons
                    buttonList.forEach { it.isEnabled = true }
                    executeNextButton()
                }
            } else {
                executeNextButton()
            }

        }

    }

    fun resetManualLayout() {
        //Make text have correct answer, make color yellow
        manual_text_box.setBackgroundResource(R.drawable.rounded_edittext)
        manual_text_box.setTextColor(Color.WHITE)
        manual_text_box.setText("")

        //reset manual layout
        right_wrong_layout.visibility = View.GONE
        manual_text_box.isEnabled = true
    }

    //Chooses the next set of words to display to user
    @SuppressLint("SetTextI18n")
    fun executeNextButton() {
        resetManualLayout()

        if(currentData.keys.size >= 6)
        {
            foreignWords = currentData.keys.toList().shuffled().take(6).toMutableList()
            //chooses a random english word that corresponds with foreign word
            val englishWordListForm = currentData[chosenForeignWords[runningGroupedScore]]!![0].shuffled().take(1)
            val englishWord = englishWordListForm.joinToString("")
            //edge case where our random selection might have included the correct answer (should be 5 wrong and we make a random one right)
            if (foreignWords.contains(chosenForeignWords[runningGroupedScore]))
            {
                foreignWords[foreignWords.indexOf(chosenForeignWords[runningGroupedScore])] = foreignWords[5]
            }

            //Chose random index to be correct
            foreignWords[(0..4).random()] = chosenForeignWords[runningGroupedScore].toString()

            topWord = englishWord


            if (!wordSwap) {
                mainWord.text = englishWord

                button1.text = foreignWords[0]
                button2.text = foreignWords[1]
                button3.text = foreignWords[2]
                button4.text = foreignWords[3]
                button5.text = foreignWords[4]
            }
            else {
                mainWord.text = chosenForeignWords[runningGroupedScore]

                button1.text = currentData[foreignWords[0]]!![0].shuffled().take(1)[0]
                button2.text = currentData[foreignWords[1]]!![0].shuffled().take(1)[0]
                button3.text = currentData[foreignWords[2]]!![0].shuffled().take(1)[0]
                button4.text = currentData[foreignWords[3]]!![0].shuffled().take(1)[0]
                button5.text = currentData[foreignWords[4]]!![0].shuffled().take(1)[0]
            }


        }
        else
        {
            mainWord.text = "Welcome!"
            button1.text = "Please add more words (6 minimum)"
            button2.text = "Please add more words (6 minimum)"
            button3.text = "Please add more words (6 minimum)"
            button4.text = "Please add more words (6 minimum)"
            button5.text = "Please add more words (6 minimum)"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
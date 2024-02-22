package com.example.notemake.Displays.HomeDisplay

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
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

    private val word1Set = mutableSetOf<String>()
    private val word2Set = mutableSetOf<String>()
    private val wordMap = mutableMapOf<String, String>()

    private var totalGrouped: Int = 3
    private var runningGroupedScore: Int = 0
    private var totalGroupedCollectionETF = mutableListOf<String>()

    private lateinit var mainWord: TextView
    private lateinit var button1: Button
    private lateinit var button2: Button
    private lateinit var button3: Button
    private lateinit var button4: Button
    private lateinit var button5: Button

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

        // Load words from SharedPreferences
        sharedPref.all.forEach { entry ->
            val wordPair = entry.key.split("|")
            val word1 = wordPair[0]
            val word2 = wordPair[1]
            word1Set.add(word1)
            word2Set.add(word2)
            if (!wordMap.containsKey(word1)) {
                wordMap[word1] = word2
            }
        }

        //Load the collection of X words. ETF = English To Foreign, FTE =  Foreign To English
        totalGroupedCollectionETF = word1Set.toList().shuffled().take(totalGrouped).toMutableList()


        setButtonListeners(view)

        createRandomWords(view,"", button1)

    }

    private fun setButtonListeners(view: View) {
        // Code to execute when buttonX is clicked
        button1.setOnClickListener {
            createRandomWords(view, mainWord.text.toString(), button1)
        }
        button2.setOnClickListener {
            createRandomWords(view, mainWord.text.toString(), button2)
        }
        button3.setOnClickListener {
            createRandomWords(view, mainWord.text.toString(), button3)
        }
        button4.setOnClickListener {
            createRandomWords(view, mainWord.text.toString(), button4)
        }
        button5.setOnClickListener {
            createRandomWords(view, mainWord.text.toString(), button5)
        }
    }

    //sets the colors of the buttons if wrong, and then calls executeNextButton method
    //also either adds to runningScore or resets score
    private fun createRandomWords(view: View, prevWord: String, chosenButton: Button) {


        val buttonList = listOf(button1, button2, button3, button4, button5)

        //WRONG ANSWER
        if(chosenButton.text.toString() != wordMap[prevWord].toString() && prevWord != "") {
            runningGroupedScore = 0
            var count = 0
            //shuffle the groupedCollection, don't let the prev word be the next upcoming word
            while (totalGroupedCollectionETF[0] == prevWord && count++ != 5 && totalGrouped != -1) {
                totalGroupedCollectionETF = totalGroupedCollectionETF.shuffled().toMutableList()
            }
            // Change colors
            buttonList.forEach {
                if (it.text.toString() == wordMap[prevWord].toString())
                    it.setBackgroundColor(Color.GREEN)
                else
                    it.setBackgroundColor(Color.RED)
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

                executeNextButton(view)
            }
        }
        //CORRECT ANSWER
        else {
            runningGroupedScore++

            //if user got the whole collection correct in one go, give them a new list
            if (runningGroupedScore == totalGroupedCollectionETF.size) {
                totalGroupedCollectionETF = word1Set.toList().shuffled().take(totalGrouped).toMutableList()
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
                    executeNextButton(view)
                }
            } else {
                executeNextButton(view)
            }

        }

    }

    //Chooses the next set of words to display to user
    @SuppressLint("SetTextI18n")
    fun executeNextButton(view: View) {
        if(wordMap.size >= 6)
        {
            val foreignWords = word2Set.toList().shuffled().take(6).toMutableList()
            val englishWord = totalGroupedCollectionETF[runningGroupedScore]

            //edge case where our random selection might have included the correct answer (should be 5 wrong and we make a random one right)
            if (foreignWords.contains(wordMap[englishWord]))
            {
                foreignWords[foreignWords.indexOf(wordMap[englishWord])] = foreignWords[5]
            }

            //Chose random index to be correct
            foreignWords[(0..4).random()] = wordMap[englishWord].toString()

            mainWord.text = englishWord

            button1.text = foreignWords[0]
            button2.text = foreignWords[1]
            button3.text = foreignWords[2]
            button4.text = foreignWords[3]
            button5.text = foreignWords[4]

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
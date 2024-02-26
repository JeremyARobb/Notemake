package com.example.notemake.Displays.EditWordsDisplay

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import com.example.notemake.databinding.FragmentSecondBinding
import android.content.Context
import android.widget.Button
import androidx.lifecycle.lifecycleScope
import com.example.notemake.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SecondFragment : Fragment() {

    private var _binding: FragmentSecondBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val sharedPref by lazy {
        activity?.getPreferences(Context.MODE_PRIVATE) ?: throw IllegalStateException("Activity cannot be null")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentSecondBinding.inflate(inflater, container, false)

        sharedPref.all.forEach { entry ->
            lifecycleScope.launch {
                val (word1, word2) = entry.value.toString().split("|")
                // Offload the creation of the word container to a background thread
                val wordColumn = withContext(Dispatchers.Default) {
                    createWordContainer(word1, word2)
                }
                // Switch back to the main thread to update the UI
                binding.newWordContainer.addView(wordColumn)
            }
        }

        binding.importButton.setOnClickListener {
            val importWords = Translator()
            importWords.englishToVietnamese655(requireContext(), sharedPref)

            //refresh display
            binding.newWordContainer.removeAllViews()
            sharedPref.all.forEach { entry ->
                lifecycleScope.launch {
                    val (word1, word2) = entry.value.toString().split("|")
                    // Offload the creation of the word container to a background thread
                    val wordColumn = withContext(Dispatchers.Default) {
                        createWordContainer(word1, word2)
                    }
                    // Switch back to the main thread to update the UI
                    binding.newWordContainer.addView(wordColumn)
                }
            }
        }

        binding.addWordButton.setOnClickListener {
            val wordFragment = WordFragment()

            wordFragment.onWordsEntered = { word1, word2 ->
                val wordColumn = createWordContainer(word1, word2)
                binding.newWordContainer.addView(wordColumn)

                // Save the word to SharedPreferences
                with(sharedPref.edit()) {
                    putString("$word1|$word2", "$word1|$word2")
                    apply()
                }
            }
            wordFragment.show(childFragmentManager, "newWordTag")
        }

        binding.buttonBack.setOnClickListener {
            findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
        }


        return binding.root
    }

    private fun createWordContainer(word1: String, word2: String): LinearLayout {

        val wordColumn = createWordColumn()
        //wordColumn.addView(RadioButton(context, null, 0, R.style.AddedRadioButton))
        val textView1 = createWord(word1)
        val textView2 = createWord(word2)
        val analyticsButton = createAnalyticsButton(wordColumn)

        wordColumn.addView(analyticsButton)
        wordColumn.addView(textView1)
        wordColumn.addView(textView2)

        val deleteButton = createDeleteButton(wordColumn, word1, word2)

        wordColumn.addView(deleteButton)

        return wordColumn
    }

    private fun createWord(word: String): TextView {
        val newTextView = TextView(context, null, 0, R.style.AddedWordTextView)
        // Set the layout weight for the newTextView to push the deleteButton to the right
        val newTextViewLayoutParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1f // This weight will make the TextView take all available space, pushing the button to the right
        )
        newTextView.layoutParams = newTextViewLayoutParams
        newTextView.text = "$word "
        return newTextView
    }

    private fun createWordColumn(): LinearLayout {
        val wordColumn = LinearLayout(context)
        wordColumn.orientation = LinearLayout.HORIZONTAL
        // Define layout parameters with a right margin
        val wordColumnParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        val wordColumnMargin = resources.getDimensionPixelSize(R.dimen.word_column_margin_size)
        wordColumnParams.setMargins(0, 0, wordColumnMargin, 0) // Set right margin
        wordColumn.layoutParams = wordColumnParams
        return wordColumn
    }

    private fun createAnalyticsButton(container: LinearLayout): Button {
        val analyticsButton = Button(context, null, 0, R.style.AnalyticsButtonStyle).apply {
            setOnClickListener {
            }
        }
        return analyticsButton
    }

    private fun createDeleteButton(container: LinearLayout, word1: String, word2: String): Button {
        val deleteButton = Button(context, null, 0, R.style.DeleteButtonStyle).apply {
            setOnClickListener {
                // Remove the word container
                binding.newWordContainer.removeView(container)

                // Delete the word from SharedPreferences
                with(sharedPref.edit()) {
                    remove("$word1|$word2")
                    apply()
                }
            }
        }
        return deleteButton
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
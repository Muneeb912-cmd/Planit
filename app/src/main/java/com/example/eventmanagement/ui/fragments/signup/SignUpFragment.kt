import android.graphics.drawable.TransitionDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.example.eventmanagement.R


class SignUpFragment : Fragment() {

    private lateinit var indicatorContainer: LinearLayout
    private lateinit var stepContainer: FrameLayout
    private lateinit var btnPrevious: Button
    private lateinit var btnNext: Button

    private var currentStep = 0
    private val totalSteps = 3

    // ViewModel or other data storage
    private val stepData = mutableMapOf<Int, String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sign_up, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        indicatorContainer = view.findViewById(R.id.indicatorContainer)
        stepContainer = view.findViewById(R.id.stepContainer)
        btnPrevious = view.findViewById(R.id.btnPrevious)
        btnNext = view.findViewById(R.id.btnNext)

        setupIndicators()
        updateStep()

        btnPrevious.setOnClickListener {
            if (currentStep > 0) {
                saveData()
                currentStep--
                updateStep()
            }
        }

        btnNext.setOnClickListener {
            if (currentStep < totalSteps - 1) {
                saveData()
                currentStep++
                updateStep()
            } else {
                // Handle finish action, save final data
                saveData()
                finishSignUp()
            }
        }
    }

    private fun setupIndicators() {
        indicatorContainer.removeAllViews()
        for (i in 0 until totalSteps) {
            val indicator = ImageView(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    marginEnd = 8.dpToPx()
                }
                setImageResource(R.drawable.indicators_inactive)
            }
            indicatorContainer.addView(indicator)
        }
        updateIndicators()
    }

    private fun updateIndicators() {
        for (i in 0 until totalSteps) {
            val indicator = indicatorContainer.getChildAt(i) as ImageView
            if (i == currentStep) {
                indicator.setImageResource(R.drawable.indicator_active)
            } else {
                indicator.setImageResource(R.drawable.indicators_inactive)
            }
        }
    }

    private fun updateStep() {
        // Clear existing content
        stepContainer.removeAllViews()

        // Load step content based on current step
        val layoutResId = when (currentStep) {
            0 -> R.layout.signup_step1
            1 -> R.layout.signup_step2
            else -> R.layout.signup_step1
        }
        val inflater = LayoutInflater.from(requireContext())
        val contentView = inflater.inflate(layoutResId, stepContainer, false)
        stepContainer.addView(contentView)

        // Restore data if available
        val inputFieldId = when (currentStep) {
            0 -> R.id.stepOneInput
            1 -> R.id.stepTwoInput
            else -> R.id.stepOneInput
        }
        val inputField = contentView.findViewById<EditText>(inputFieldId)
        inputField.setText(stepData[currentStep])

        // Update button visibility and text
        btnPrevious.visibility = if (currentStep == 0) View.GONE else View.VISIBLE
        btnNext.text = if (currentStep == totalSteps - 1) "Finish" else "Next"

        // Animate background transition
        val transitionDrawable = stepContainer.background as TransitionDrawable
        transitionDrawable.startTransition(300) // Duration of the transition

        // Update indicators
        updateIndicators()
    }

    private fun saveData() {
        val inputFieldId = when (currentStep) {
            0 -> R.id.stepOneInput
            1 -> R.id.stepTwoInput
            else -> R.id.stepOneInput
        }
        val inputField = stepContainer.findViewById<EditText>(inputFieldId)
        stepData[currentStep] = inputField.text.toString()
    }

    private fun finishSignUp() {
        // Handle final data processing and sign-up completion
    }

    private fun Int.dpToPx(): Int {
        val density = resources.displayMetrics.density
        return (this * density).toInt()
    }
}

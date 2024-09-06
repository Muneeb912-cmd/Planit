package com.example.eventmanagement.ui.fragments.login

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.eventmanagement.R
import com.example.eventmanagement.databinding.FragmentLoginBinding
import com.example.eventmanagement.models.User
import com.example.eventmanagement.ui.shared_view_model.SharedViewModel
import com.example.eventmanagement.receivers.ConnectivityObserver
import com.example.eventmanagement.utils.Response
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding
    private val viewModel: LoginViewModel by viewModels()
    private val sharedViewModel: SharedViewModel by activityViewModels()
    @Inject
    lateinit var connectivityObserver: ConnectivityObserver

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
        checkUserPrefs()
    }


    private fun checkUserPrefs() {
        viewModel.checkUserInPrefs { userExists, userRole ->
            if (userExists) {
                navigateBasedOnUserRole(userRole)
            }
        }
    }

    private fun setupListeners() {
        with(binding) {
            signupBtn.setOnClickListener { navigateToSignUp("email_pass") }
            forgotPassTv.setOnClickListener { navigateToForgotPassword() }
            googleLoginBtn.setOnClickListener { signInWithGoogle() }
            loginBtn.setOnClickListener {
                val email = email.text.toString()
                val password = password.text.toString()
                handleLoginWithEmailPassword(email, password)
            }
        }
    }

    private fun navigateToSignUp(loginType: String) {
        val action = LoginFragmentDirections.actionLoginFragmentToSignUpFragment(loginType)
        findNavController().navigate(action)
    }

    private fun navigateToForgotPassword() {
        findNavController().navigate(R.id.action_loginFragment_to_forgetPasswordFragment)
    }

    private fun navigateBasedOnUserRole(userRole: String) {
        val destination = when (userRole) {
            "Attendee" -> R.id.action_loginFragment_to_eventsMainFragment
            "Admin" -> R.id.action_loginFragment_to_adminMainFragment
            else -> return
        }

        findNavController().navigate(
            destination,
            null,
            NavOptions.Builder()
                .setPopUpTo(R.id.nav_graph_xml, true)
                .setLaunchSingleTop(true)
                .build()
        )
    }

    private fun signInWithGoogle() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.client_server_key))
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(requireContext(), gso)
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
        observeViewModel("google")
    }

    private fun handleLoginWithEmailPassword(email: String, password: String) {
        viewModel.signInWithEmailPassword(email, password)
        observeViewModel("email_pass")
    }

    private fun observeViewModel(loginType: String) {
        lifecycleScope.launch {
            viewModel.loginResult.collect { response ->
                handleLoginResponse(response, loginType)
            }
        }
    }

    private fun handleLoginResponse(response: Response<Unit>, loginType: String) {
        when (response) {
            is Response.Loading -> showLoader(true)
            is Response.Success -> handleLoginSuccess(loginType)
            is Response.Error -> {
                showLoader(false)
                showToast("Error Occurred: ${response.exception.message}")
            }
        }
    }

    private fun handleLoginSuccess(loginType: String) {
        lifecycleScope.launch {
            viewModel.isEmailVerified.collect { isVerified ->
                showLoader(false)
                if (isVerified) {
                    handleUserVerificationSuccess(loginType)
                } else {
                    showToast("Please verify your email")
                }
            }
        }
    }

    private fun handleUserVerificationSuccess(loginType: String) {
        lifecycleScope.launch {
            viewModel.usersData.collect { usersResponse ->
                when (usersResponse) {
                    is Response.Loading -> Unit
                    is Response.Success -> handleUserExists(usersResponse.data, loginType)
                    is Response.Error -> showToast("Error fetching users: ${usersResponse.exception.message}")
                }
            }
        }
    }

    private fun handleUserExists(users: List<User.UserData>, loginType: String) {
        val currentUser = viewModel.getCurrentUser()
        val currentUserId = currentUser?.userId

        if (currentUserId != null) {
            val user = users.firstOrNull { it.userId == currentUserId }
            sharedViewModel.initializeObservers()
            user?.let {
                if (it.userBanned == true) {
                    handleBannedUser()
                } else {
                    saveUserAndNavigate(it)
                }
            } ?: navigateToSignUp(loginType)
        } else {
            showToast("User Don't Exist")
        }
    }

    private fun handleBannedUser() {
        showAlertDialog(
            "Attention",
            "Your account is suspended by admin. Kindly contact the admin for more information at:\n\n dukocommunity@gmail.com",
            R.drawable.ic_attention
        )
        clearInputFields()
    }

    private fun clearInputFields() {
        binding.email.text?.clear()
        binding.password.text?.clear()
        binding.password.clearFocus()
    }

    private fun saveUserAndNavigate(user: User.UserData) {
        viewModel.saveDataToPreferences(user) { isUserSavedInPrefs ->
            if (isUserSavedInPrefs) {
                showToast("User instance saved for seamless login")
                user.userRole?.let { navigateBasedOnUserRole(it) }
            } else {
                showToast("Couldn't save user instance")
            }
        }
    }

    private fun handleSignInWithGoogle(account: GoogleSignInAccount) {
        viewModel.signInWithGoogle(account)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                account?.let { handleSignInWithGoogle(it) }
            } catch (e: ApiException) {
                showLoader(false)
                showToast("Google Sign-In failed: ${e.message}")
            }
        }
    }

    private fun showLoader(show: Boolean) {
        view?.findViewById<FrameLayout>(R.id.loader_overlay)?.visibility =
            if (show) View.VISIBLE else View.GONE
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun showAlertDialog(title: String, message: String, icon: Int) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setIcon(icon)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    companion object {
        private const val RC_SIGN_IN = 9001
    }
}

package com.example.eventmanagement.ui.fragments.login

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.eventmanagement.R
import com.example.eventmanagement.databinding.FragmentLoginBinding
import com.example.eventmanagement.utils.Response
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding
    private val viewModel: LoginViewModel by viewModels()

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
        viewModel.checkUserInPrefs { userExists ->
            if (userExists) {
                findNavController().navigate(
                    R.id.action_loginFragment_to_eventsMainFragment,
                    null,
                    NavOptions.Builder()
                        .setPopUpTo(R.id.nav_graph_xml, true)
                        .setLaunchSingleTop(true)
                        .build()
                )
            }
        }
    }

    private fun setupListeners() {
        binding.signupBtn.setOnClickListener {
            val action = LoginFragmentDirections.actionLoginFragmentToSignUpFragment("email_pass")
            findNavController().navigate(action)
        }

        binding.forgotPassTv.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_forgetPasswordFragment)
        }

        binding.googleLoginBtn.setOnClickListener {
            signInWithGoogle()
        }

        binding.loginBtn.setOnClickListener {
            val email = binding.email.text.toString()
            val password = binding.password.text.toString()
            handleLoginWithEmailPassword(email, password)
        }
    }

    private fun observeViewModel(loginType: String) {
        lifecycleScope.launch {
            viewModel.loginResult.collect { response ->
                when (response) {
                    is Response.Loading -> showLoader(true)
                    is Response.Success -> {
                        viewModel.isEmailVerified.collect { isVerified ->
                            showLoader(false)
                            if (isVerified) {
                                viewModel.usersData.collect { usersResponse ->
                                    when (usersResponse) {
                                        is Response.Loading -> {
                                            // Optionally handle loading state
                                        }

                                        is Response.Success -> {
                                            val users = usersResponse.data
                                            val currentUser = viewModel.getCurrentUser()
                                            val currentUserId = currentUser?.userId

                                            if (currentUserId != null) {
                                                viewModel.getUserDataFromFireStore(currentUserId) { isUserSavedInPrefs ->
                                                    if (isUserSavedInPrefs) {
                                                        Toast.makeText(
                                                            requireContext(),
                                                            "User instance saved for seamless login",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    } else {
                                                        Toast.makeText(
                                                            requireContext(),
                                                            "Couldn't save user instance",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                }
                                            }

                                            if (currentUserId != null) {
                                                val userExists =
                                                    users.any { it.userId == currentUserId }
                                                if (userExists) {
                                                    findNavController().navigate(R.id.action_loginFragment_to_eventsMainFragment)
                                                } else {
                                                    val action =
                                                        LoginFragmentDirections.actionLoginFragmentToSignUpFragment(
                                                            loginType
                                                        )
                                                    findNavController().navigate(action)
                                                }
                                            } else {
                                                Toast.makeText(
                                                    requireContext(),
                                                    "User Don't Exist",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }

                                        is Response.Error -> {
                                            Toast.makeText(
                                                requireContext(),
                                                "Error fetching users: ${usersResponse.exception.message}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                }
                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    "Please verify your email",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }

                    is Response.Error -> {
                        showLoader(false)
                        Toast.makeText(
                            requireContext(),
                            "Error Occurred: ${response.exception.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
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

    private fun handleSignInWithGoogle(account: GoogleSignInAccount) {
        viewModel.signInWithGoogle(account)
    }

    private fun showLoader(show: Boolean) {
        view?.findViewById<FrameLayout>(R.id.loader_overlay)?.visibility =
            if (show) View.VISIBLE else View.GONE
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                if (account != null) {
                    handleSignInWithGoogle(account)
                }
            } catch (e: ApiException) {
                showLoader(false)
                Toast.makeText(
                    requireContext(),
                    "Google Sign-In failed: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    companion object {
        private const val RC_SIGN_IN = 9001
    }
}

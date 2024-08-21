package com.example.eventmanagement.di

import android.content.Context
import com.example.eventmanagement.repository.firebase.envents_data.EventDataMethods
import com.example.eventmanagement.repository.firebase.envents_data.EventsDataMethodsImpl
import com.example.eventmanagement.repository.firebase.invites_data.InviteMethods
import com.example.eventmanagement.repository.firebase.invites_data.InviteMethodsImpl
import com.example.eventmanagement.repository.firebase.login_signup.LoginSignUpMethods
import com.example.eventmanagement.repository.firebase.login_signup.LoginSignUpMethodsImpl
import com.example.eventmanagement.repository.firebase.user_data.UserDataImpl
import com.example.eventmanagement.repository.firebase.user_data.UserDataMethods
import com.example.eventmanagement.utils.PreferencesUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class FirebaseModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage = FirebaseStorage.getInstance()

    @Provides
    @Singleton
    fun provideGoogleSignInClient(@ApplicationContext context: Context): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(context, gso)
    }

    @Provides
    @Singleton
    fun provideFirebaseMethods(
        auth: FirebaseAuth,
        firestore: FirebaseFirestore,
        firebaseStorage: FirebaseStorage
    ): LoginSignUpMethods = LoginSignUpMethodsImpl(auth, firestore, firebaseStorage)


    @Provides
    @Singleton
    fun provideUserDataMethods(
        auth: FirebaseAuth,
        firestore: FirebaseFirestore,
        preferencesUtil: PreferencesUtil,
        firebaseStorage: FirebaseStorage
    ): UserDataMethods = UserDataImpl(auth, firestore, preferencesUtil,firebaseStorage)

    @Provides
    @Singleton
    fun provideEventDataMethods(
        firestore: FirebaseFirestore
    ): EventDataMethods = EventsDataMethodsImpl(firestore)

    @Provides
    @Singleton
    fun provideInviteMethods(
        auth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): InviteMethods = InviteMethodsImpl(auth, firestore)
}

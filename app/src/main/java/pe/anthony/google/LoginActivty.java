package pe.anthony.google;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInApi;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.auth.api.signin.SignInAccount;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiActivity;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivty extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient googleApiClient; //Esto es el intermedio entre las APLI de google y est aplicacion
    private SignInButton signInButton;
    private static final int  IntentSignIn = 777;

//    Esto ya es para una autentificacion con fireBase
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;
    private ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_activty);
//        Este es un obeto de opciones que nos dira como autenticarnos por  default y si ademas queremos el correo de la persona .requestEmail()
//       .requestIdToken(getString(R.string.default_web_client_id)) es para obtener un token
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

//        Inicializo el metodo
//        enableAutoManage.- Esto me permite gestionar el ciclo de vida del googleApliClient con el del Activity
//        enableAutoManage recive como parametro el primero es la activity y el segundo es quien se encargara de escuchar si algo sale mal
//        addApi(Auth.GOOGLE_SIGN_IN_API,) .- Es para la autentificacion con Google

        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage( this,this)
                .addApi(Auth.GOOGLE_SIGN_IN_API,googleSignInOptions)
                .build();

        signInButton = findViewById(R.id.signInButton);
//        Personalizacion del boton
        signInButton.setSize(SignInButton.SIZE_WIDE);
        signInButton.setColorScheme(SignInButton.COLOR_DARK);

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
                startActivityForResult(intent, IntentSignIn);
            }
        });

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
//            Recuerda que este metodo se ejecuta cuando el oyente escucha algun cambio de estado
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user!= null){
                    goMainActivity();
                }
            }
        };
        progressBar = findViewById(R.id.progressBar);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        Gracias al intent data podemos manejar el resultado
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == IntentSignIn){
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);   //Aqui obtengo el resultado
            handleSignInResult(result);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        if(result.isSuccess()){
            firebaseAuthWithGoogle(result.getSignInAccount());
//            goMainActivity();
        }else{
            Toast.makeText(this,R.string.not_log_in,Toast.LENGTH_SHORT).show();
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount signInAccount) {
        progressBar.setVisibility(View.VISIBLE);
        signInButton.setVisibility(View.GONE);
        AuthCredential credential = GoogleAuthProvider.getCredential(signInAccount.getIdToken(),null);
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
//            Recuerda que este es el metodo que se ejecuta cuando el oyente terminar
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
             if(!task.isSuccessful()){
                 Toast.makeText(getApplicationContext(),R.string.not_firebase_auth,Toast.LENGTH_SHORT).show();
             }
            }
        });
    }

    private void goMainActivity() {
        Intent intent = new Intent(this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        //Este metodo muestra si en caso falla al momento de conectarte con google
    }

    @Override
    protected void onStart() {
        super.onStart();
        //Aca estoy diciendo el momento que debe empezar a escuchar el oyente
        firebaseAuth.addAuthStateListener(firebaseAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        //Aca estoy diciendo cuando debe detenerse de escuchar
        if(firebaseAuthListener != null){
            firebaseAuth.removeAuthStateListener(firebaseAuthListener);
        }
    }
}

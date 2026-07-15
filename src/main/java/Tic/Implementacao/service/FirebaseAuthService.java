package Tic.Implementacao.service;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import org.springframework.stereotype.Service;

@Service
public class FirebaseAuthService {

    public String criarUsuario(String nome, String email, String senha) {
        validarFirebaseConfigurado();

        UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                .setDisplayName(nome)
                .setEmail(email)
                .setPassword(senha)
                .setEmailVerified(false)
                .setDisabled(false);

        try {
            return FirebaseAuth.getInstance().createUser(request).getUid();
        } catch (FirebaseAuthException e) {
            throw new RuntimeException("Erro ao criar usuário no Firebase: " + e.getMessage(), e);
        }
    }

    public void excluirUsuario(String firebaseUid) {
        validarFirebaseConfigurado();
        if (firebaseUid == null || firebaseUid.isBlank()) return;

        try {
            FirebaseAuth.getInstance().deleteUser(firebaseUid);
        } catch (FirebaseAuthException e) {
            throw new RuntimeException("Erro ao excluir usuário no Firebase: " + e.getMessage(), e);
        }
    }

    private void validarFirebaseConfigurado() {
        if (FirebaseApp.getApps().isEmpty()) {
            throw new RuntimeException("Firebase Admin não configurado. Defina firebase.credentials.path.");
        }
    }
}

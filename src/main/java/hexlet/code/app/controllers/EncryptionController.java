package hexlet.code.app.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.security.crypto.keygen.KeyGenerators;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/encryption")
public class EncryptionController {

    @PostMapping(path = "/encrypt")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, String> encrypt(@RequestParam String clearText, @RequestParam String password) throws IOException {
        String generatedSalt = KeyGenerators.string().generateKey();

        TextEncryptor encryptor = Encryptors.delux(password, generatedSalt);
        String encryptedPem = encryptor.encrypt(clearText);

        return Map.of("result", encryptedPem, "salt", generatedSalt);
    }

    @PostMapping(path = "/decrypt")
    @ResponseStatus(HttpStatus.CREATED)
    public String decrypt(
            @RequestParam String password,
            @RequestParam String salt,
            @RequestParam String encryptedText
    ) throws IOException {
        TextEncryptor decryptor = Encryptors.delux(password, salt);
        String clearText = decryptor.decrypt(encryptedText);

        return clearText;
    }
}

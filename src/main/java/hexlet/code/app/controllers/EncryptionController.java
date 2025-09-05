package hexlet.code.app.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

@RestController
@RequestMapping("/encryption")
public class EncryptionController {

    @PostMapping(path = "/encrypt")
    @ResponseStatus(HttpStatus.CREATED)
    public String encrypt(@RequestParam String password, @RequestParam String salt) throws IOException {
        var srcFile = ResourceUtils.getFile("classpath:certs/private.pem");
        String srcPem = Files.readString(srcFile.toPath(), StandardCharsets.UTF_8);

//        String generatedSalt = KeyGenerators.string().generateKey();

        TextEncryptor encryptor = Encryptors.delux(password, salt);
        String encryptedPem = encryptor.encrypt(srcPem);

        return encryptedPem;
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

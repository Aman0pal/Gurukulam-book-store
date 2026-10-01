package GURUKULAM.example.BOOK_STORE;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    @Autowired
    private Cloudinary cloudinary;

    public String uploadFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        try {
            // Upload the file to Cloudinary. Setting resource_type to "auto" handles PDFs.
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap("resource_type", "auto"));
            if (uploadResult != null && uploadResult.get("secure_url") != null) {
                return uploadResult.get("secure_url").toString();
            }
            return null;
        } catch (Exception e) {
            System.err.println("Error uploading file to Cloudinary: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}

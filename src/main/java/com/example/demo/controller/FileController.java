package com.example.demo.controller;

import com.alibaba.fastjson.JSONObject;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

@RestController
@RequestMapping("/api/files")
public class FileController {

    @Value("${aliyun.api.endpoint}")
    private String aliyunApiEndpoint;

    @Value("${aliyun.access.key.id}")
    private String accessKeyId;

    @Value("${aliyun.access.key.secret}")
    private String accessKeySecret;

    @Value("${aliyun.app.key}")
    private String appKey;

    @Value("${aliyun.oss.endpoint}")
    private String ossEndpoint;

    @Value("${aliyun.oss.bucket.name}")
    private String ossBucketName;

    // 识别请求接口
    @PostMapping("/recognize")
    public ResponseEntity<String> recognizeAudio(@RequestParam("file") MultipartFile file) {
        String fileUrl;
        try {
            // 上传文件到阿里云OSS并获取URL
            fileUrl = uploadToOSS(file);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("无法上传文件: " + e.getMessage());
        }

        String taskId;
        try {
            // 调用阿里云识别API
            taskId = callAliyunRecognitionApi(fileUrl);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("调用阿里云识别API失败: " + e.getMessage());
        }

        return ResponseEntity.ok("Task submitted successfully. TaskId: " + taskId);
    }

    private String uploadToOSS(MultipartFile file) throws Exception {
        OSS ossClient = new OSSClientBuilder().build(ossEndpoint, accessKeyId, accessKeySecret);
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());

        // 上传文件到OSS，设置Content-Type
        PutObjectRequest putObjectRequest = new PutObjectRequest(ossBucketName, originalFileName, file.getInputStream());
        putObjectRequest.setMetadata(new ObjectMetadata()); // 设置元数据
        putObjectRequest.getMetadata().setContentType(file.getContentType()); // 设置Content-Type
        ossClient.putObject(putObjectRequest);

        // 获取文件的公共可访问URL
        String fileUrl = "https://" + ossBucketName + "." + ossEndpoint + "/" + originalFileName;
        ossClient.shutdown();

        return fileUrl;
    }

    private String callAliyunRecognitionApi(String audioUrl) throws Exception {
        String url = aliyunApiEndpoint + "/openapi/tingwu/v2/tasks";
        String method = "PUT"; // HTTP 方法
        String uri = "/openapi/tingwu/v2/tasks"; // 请求的 URI
        String requestBody = "{ \"Input\": { \"FileUrl\": \"" + audioUrl + "\" }, \"AppKey\": \"" + appKey + "\" }"; // 请求参数
        String date = getCurrentDate(); // 当前日期

        System.out.println("Request URL: " + url);
        System.out.println("Request Body: " + requestBody);
        System.out.println("Request Date: " + date);

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPut put = new HttpPut(url);
            put.setHeader("Content-Type", "application/json");
            put.setHeader("Date", date); // 需要添加日期头部

            // 生成签名
            String signature = generateSignature(method, uri, requestBody, date, accessKeySecret);
            System.out.println("Access Key ID: " + accessKeyId); // 调试信息
            System.out.println("Signature: " + signature); // 调试信息
            put.setHeader("Authorization", "AccessKey " + accessKeyId + "/" + signature);

            put.setEntity(new StringEntity(requestBody, "UTF-8"));

            HttpResponse response = httpClient.execute(put);
            BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            StringBuilder responseString = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                responseString.append(line);
            }

            System.out.println("Response: " + responseString.toString());

            JSONObject json = JSONObject.parseObject(responseString.toString());
            String taskId = json.getJSONObject("Data").getString("TaskId");
            return taskId;
        }
    }


    private String generateSignature(String method, String uri, String params, String date, String accessKeySecret) {
        try {
            // 生成签名字符串
            String stringToSign = method + "\n" + uri + "\n" + params + "\n" + date;
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(accessKeySecret.getBytes("UTF-8"), "HmacSHA256");
            mac.init(keySpec);
            byte[] signatureBytes = mac.doFinal(stringToSign.getBytes("UTF-8"));
            return Base64.getEncoder().encodeToString(signatureBytes);
        } catch (Exception e) {
            throw new RuntimeException("签名生成失败", e);
        }
    }


    // 获取当前日期的方法
    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        return sdf.format(new Date());
    }

}

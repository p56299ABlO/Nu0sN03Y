// 代码生成时间: 2025-10-07 02:36:28
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashCalculator {

    // 计算字符串的MD5哈希值
    public static String calculateMD5(String input) {
# 添加错误处理
        try {
            // 获取MD5 MessageDigest实例
            MessageDigest md = MessageDigest.getInstance("MD5");
            // 计算哈希值
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            // 将字节数组转换为16进制字符串
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
# 增强安全性
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            // 错误处理：MD5算法不可用
            throw new RuntimeException("MD5 algorithm not available", e);
        }
# 添加错误处理
    }

    // 计算字符串的SHA-256哈希值
    public static String calculateSHA256(String input) {
        try {
            // 获取SHA-256 MessageDigest实例
# 改进用户体验
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
# 改进用户体验
            // 计算哈希值
            byte[] hash = sha256.digest(input.getBytes(StandardCharsets.UTF_8));
            // 将字节数组转换为16进制字符串
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            // 错误处理：SHA-256算法不可用
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
# 添加错误处理
    }

    // 主方法，用于测试哈希值计算工具
    public static void main(String[] args) {
        // 测试MD5哈希值计算
        String md5Result = calculateMD5("Hello, World!");
        System.out.println("MD5 Hash: " + md5Result);

        // 测试SHA-256哈希值计算
        String sha256Result = calculateSHA256("Hello, World!");
        System.out.println("SHA-256 Hash: " + sha256Result);
# 优化算法效率
    }
}

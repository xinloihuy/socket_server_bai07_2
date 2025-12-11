package org.example;

import java.net.*;
import java.io.*;

/**
 * Lớp chính của Socket Server, chịu trách nhiệm khởi động và lắng nghe kết nối.
 */
public class LightControlServer {

    // Cổng (Port) mà Server sẽ lắng nghe. Phải giống với cổng mà Client Android kết nối tới.
    private static final int PORT = 12345;

    public static void main(String[] args) throws IOException {
        // Khai báo ServerSocket (Máy chủ)
        ServerSocket serverSocket = null;
        // Nếu bạn muốn buộc Server chỉ lắng nghe trên 10.10.10.10
        String specificIp = "10.10.10.10";
        InetAddress bindAddr = InetAddress.getByName(specificIp);
        // Tạo ServerSocket chỉ lắng nghe trên địa chỉ IP này
        serverSocket = new ServerSocket(PORT, 50, bindAddr);

        try {
            // 1. Tạo ServerSocket và gắn vào cổng đã định nghĩa
            serverSocket = new ServerSocket(PORT);
            System.out.println("==================================================");
            System.out.println("Máy chủ Đèn đã Khởi Động tại Cổng: " + PORT);
            System.out.println("Địa chỉ IP của máy chủ (dùng cho Client Android): " + InetAddress.getLocalHost().getHostAddress());
            System.out.println("Đang chờ Client kết nối...");
            System.out.println("==================================================");

            // Server chạy liên tục để lắng nghe kết nối mới
            while (true) {
                // 2. Chờ Client kết nối. Lệnh này sẽ bị block cho đến khi có Client.
                Socket clientSocket = serverSocket.accept();
                System.out.println("\n[KẾT NỐI MỚI] Client đã kết nối từ: " + clientSocket.getInetAddress().getHostAddress());

                // Khởi động một luồng mới (ClientHandler) để xử lý giao tiếp
                // Điều này cho phép Server tiếp tục lắng nghe các kết nối khác.
                new ClientHandler(clientSocket).start();
            }

        } catch (IOException e) {
            System.err.println("Lỗi khởi động Server. Đảm bảo cổng " + PORT + " chưa được sử dụng.");
            e.printStackTrace();
        } finally {
            // Đóng ServerSocket
            if (serverSocket != null && !serverSocket.isClosed()) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

/**
 * Lớp xử lý giao tiếp với từng Client cụ thể.
 * Chạy trong một luồng riêng biệt (Thread).
 */
class ClientHandler extends Thread {
    private Socket clientSocket;

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try (
                // Dùng BufferedReader để đọc dữ liệu từ Client (InputStream)
                BufferedReader input = new BufferedReader(
                        new InputStreamReader(clientSocket.getInputStream()));

                // Dùng PrintWriter để gửi dữ liệu về Client (OutputStream)
                PrintWriter output = new PrintWriter(
                        clientSocket.getOutputStream(), true); // 'true' để tự động flush

        ) {
            String clientMessage;

            // 3. Đọc dữ liệu từ Client theo từng dòng
            // Giả định Client chỉ gửi một lệnh và đóng kết nối sau đó.
            if ((clientMessage = input.readLine()) != null) {
                System.out.println("   -> Nhận lệnh: " + clientMessage);

                String responseMessage;
                String command = clientMessage.trim().toUpperCase();

                // 4. Xử lý lệnh bật/tắt đèn
                if (command.equals("ON")) {
                    responseMessage = "SUCCESS: Đèn đã BẬT";
                    System.out.println("   >>> TRẠNG THÁI: ĐÈN ĐÃ BẬT! <<<");
                } else if (command.equals("OFF")) {
                    responseMessage = "SUCCESS: Đèn đã TẮT";
                    System.out.println("   >>> TRẠNG THÁI: ĐÈN ĐÃ TẮT! <<<");
                } else {
                    responseMessage = "ERROR: Lệnh khong hop le. Hay gui 'ON' hoac 'OFF'.";
                    System.err.println("   Lỗi: Nhận lệnh không hợp lệ.");
                }

                // Gửi phản hồi về cho Client Android
                output.println(responseMessage);
            }

        } catch (IOException e) {
            System.err.println("   Lỗi xử lý Client " + clientSocket.getInetAddress().getHostAddress() + ": " + e.getMessage());
        } finally {
            // 5. Đóng Socket của Client
            try {
                if (clientSocket != null) {
                    clientSocket.close();
                    System.out.println("   [KẾT NỐI ĐÓNG] Giao tiếp với Client đã kết thúc.");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
# STEP 1: Sử dụng một base image có chứa Java Development Kit (JDK) để biên dịch code.
# OpenJDK 17 là một lựa chọn phổ biến và ổn định.
FROM openjdk:17-jdk-slim AS builder

# Thiết lập thư mục làm việc bên trong container
WORKDIR /app

# Sao chép toàn bộ mã nguồn (.java) từ máy của bạn vào thư mục /app trong container
COPY src/ .

# Biên dịch tất cả các file .java
# Lệnh này sẽ tìm tất cả file có đuôi .java và biên dịch chúng, 
# kết quả *.class sẽ được đặt đúng trong cấu trúc thư mục (ví dụ: org/example/*.class)
RUN javac -d . $(find . -name "*.java")


# STEP 2: Sử dụng một base image nhỏ hơn chỉ chứa Java Runtime Environment (JRE) để chạy ứng dụng.
# Điều này giúp image cuối cùng có dung lượng nhỏ hơn và an toàn hơn.
FROM openjdk:17-jre-slim

# Thiết lập thư mục làm việc
WORKDIR /app

# Sao chép các file đã được biên dịch (*.class) từ stage 'builder'
COPY --from=builder /app/org ./org

# Cổng (PORT) mà ứng dụng của bạn sẽ lắng nghe bên trong container.
# Phải khớp với cổng trong code Java của bạn (12345).
EXPOSE 12345

# Lệnh để khởi chạy ứng dụng khi container bắt đầu.
# Docker sẽ thực thi: java org.example.LightControlServer
CMD ["java", "org.example.LightControlServer"]

# MySQL cho TestingSystem

Chạy từ thư mục gốc dự án:

```powershell
docker compose -f docker/docker-compose.yml up -d --wait
```

Kết nối theo `hibernate.cfg.xml`: `localhost:3306`, database `TestingSystem`, user `root`, password `123456`. Spring Boot chạy trên máy host.

MySQL tự chạy `01_init.sql` rồi `02_inser_data.sql` trong lần khởi tạo volume đầu tiên. Volume `mysql_data` giữ dữ liệu khi dừng hoặc tạo lại container; sửa SQL không tự chạy lại trên volume đã có dữ liệu.

Xem trạng thái và log:

```powershell
docker compose -f docker/docker-compose.yml ps
docker compose -f docker/docker-compose.yml logs -f mysql
```

Dừng và giữ dữ liệu:

```powershell
docker compose -f docker/docker-compose.yml down
```

Nếu muốn xóa toàn bộ dữ liệu của Compose này để khởi tạo lại từ SQL (chỉ chạy khi chấp nhận mất dữ liệu):

```powershell
docker compose -f docker/docker-compose.yml down -v
docker compose -f docker/docker-compose.yml up -d --wait
```

Cổng 3306 trên máy phải còn trống; nếu MySQL local đang chạy ở cổng này, dừng MySQL local trước khi chạy Compose.

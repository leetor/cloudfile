package name.zjq.blog.pcd.download;

import java.math.BigDecimal;
import java.util.Map;

public interface DLInterface {
	// 删除下载
	void delete();

	// 暂停下载
	void stop();

	// 获取下载状态
	Map<String, String> getStatus();

	default String calculateDescSize(long filesize) {
		BigDecimal big1 = new BigDecimal(filesize);
		if (filesize > 1073741824) {
			return big1.divide(new BigDecimal(1073741824), 2, BigDecimal.ROUND_HALF_EVEN).toString() + "GB";
		} else if (filesize > 1024 * 1024) {
			return big1.divide(new BigDecimal(1024 * 1024), 2, BigDecimal.ROUND_HALF_EVEN).toString() + "MB";
		} else {
			return big1.divide(new BigDecimal(1024), 2, BigDecimal.ROUND_HALF_EVEN).toString() + "KB";
		}
	}
}

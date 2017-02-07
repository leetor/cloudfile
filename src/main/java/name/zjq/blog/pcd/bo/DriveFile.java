package name.zjq.blog.pcd.bo;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import name.zjq.blog.pcd.utils.Coder;

public class DriveFile {
	private static final Log logger = LogFactory.getLog(DriveFile.class);

	private boolean isDir;// 是否是目录
	private String fileType = "";// 文件类型
	private String fileName;// 文件名称
	private String base64filepath;// base64编码文件地址
	private long fileSize;// 文件大小(单位:bytes)
	private String descSize;// 描述大小
	private String lastModifiedTime;// 文件最后修改时间

	public DriveFile(String maindir, Path fileArg, BasicFileAttributes attrs) {
		this.isDir = attrs.isDirectory();
		this.fileName = fileArg.getFileName().toString();
		if (this.isDir) {
			this.fileType = "文件夹";
			this.descSize = "";
		} else {
			if (fileName.lastIndexOf(".") > -1) {
				this.fileType = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
			}
			this.descSize = calculateDescSize();
		}
		this.fileSize = attrs.size();
		String filepath = fileArg.toAbsolutePath().toString().replace("\\", "/").replace(maindir, "");
		this.base64filepath = Coder.encoderURLBASE64((filepath).getBytes());
		this.lastModifiedTime = formatTime(attrs.lastModifiedTime().toMillis());
	}

	private String calculateDescSize() {
		BigDecimal big1 = new BigDecimal(this.fileSize);
		if (this.fileSize > 1073741824) {
			return big1.divide(new BigDecimal(1073741824), 2, BigDecimal.ROUND_HALF_EVEN).toString() + "GB";
		} else if (this.fileSize > 1024 * 1024) {
			return big1.divide(new BigDecimal(1024 * 1024), 2, BigDecimal.ROUND_HALF_EVEN).toString() + "MB";
		} else {
			return big1.divide(new BigDecimal(1024), 2, BigDecimal.ROUND_HALF_EVEN).toString() + "KB";
		}
	}

	public boolean isDir() {
		return isDir;
	}

	public String getFileType() {
		return fileType;
	}

	public String getFileName() {
		return fileName;
	}

	public String getDescSize() {
		return descSize;
	}

	public String getLastModifiedTime() {
		return lastModifiedTime;
	}

	public String getBase64filepath() {
		return base64filepath;
	}

	private static String formatTime(long time) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return formatter.format(new Date(time));
	}

	/**
	 * 获取文件列表
	 * 
	 * @param filepath
	 * @return
	 * @throws IOException
	 */
	public static List<DriveFile> getFileList(String maindir, String filepath) {
		List<DriveFile> filelsit = new ArrayList<DriveFile>();
		List<DriveFile> dirlsit = new ArrayList<DriveFile>();
		try {
			Files.walkFileTree(Paths.get(filepath), new HashSet<FileVisitOption>(), 1, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					DriveFile df = new DriveFile(maindir, file, attrs);
					if (attrs.isDirectory()) {
						dirlsit.add(df);
					} else {
						filelsit.add(df);
					}
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (IOException e) {
			logger.error(e);
		}
		dirlsit.addAll(filelsit);
		return dirlsit;
	}

	/**
	 * 删除文件
	 * 
	 * @param filepath
	 * @return
	 * @throws FileNotFoundException
	 */
	public static boolean delFile(String filepath) throws FileNotFoundException {
		Path file = Paths.get(filepath);
		boolean pathExists = Files.exists(file, new LinkOption[] { LinkOption.NOFOLLOW_LINKS });
		if (!pathExists) {
			throw new FileNotFoundException("文件不存在");
		} else {
			try {
				Files.walkFileTree(file, new SimpleFileVisitor<Path>() {
					@Override
					public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
						Files.delete(file);
						return FileVisitResult.CONTINUE;
					}

					@Override
					public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
						Files.delete(dir);
						return FileVisitResult.CONTINUE;
					}
				});
			} catch (IOException e) {
				logger.error(e);
				return false;
			}
			return true;
		}
	}

	/**
	 * 文件重命名
	 * 
	 * @param filepath
	 * @param newfilename
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws FileAlreadyExistsException
	 */
	public static boolean renameFile(String filepath, String newfilename)
			throws FileNotFoundException, IOException, FileAlreadyExistsException {
		newfilename = new String(Coder.decoderURLBASE64(newfilename), "utf-8");
		Path source = Paths.get(filepath);
		boolean pathExists = Files.exists(source, new LinkOption[] { LinkOption.NOFOLLOW_LINKS });
		if (!pathExists) {
			throw new FileNotFoundException("文件不存在");
		} else {
			Path target = source.resolveSibling(newfilename);
			if (source.equals(target)) {
				throw new FileAlreadyExistsException("新文件命名与旧文件命名相同，已取消重命名操作");
			}
			try {
				Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
				return true;
			} catch (IOException e) {
				logger.error(e);
				return false;
			}
		}

	}

	/**
	 * 创建新文件
	 * 
	 * @param filepath
	 * @param newfilename
	 * @return
	 * @throws IOException
	 */
	public static boolean createNewFile(String filepath, String newfilename) throws IOException {
		newfilename = new String(Coder.decoderURLBASE64(newfilename), "utf-8");
		Path source = Paths.get(filepath, newfilename);
		boolean pathExists = Files.exists(source, new LinkOption[] { LinkOption.NOFOLLOW_LINKS });
		if (pathExists) {
			throw new IOException("文件已存在！");
		} else {
			try {
				Files.createFile(source);
			} catch (IOException e) {
				logger.error(e);
				return false;
			}
			return true;
		}
	}

	/**
	 * 创建新文件夹
	 * 
	 * @param filepath
	 * @param newfilename
	 * @return
	 * @throws IOException
	 */
	public static boolean createNewDir(String filepath, String newfilename) throws IOException {
		newfilename = new String(Coder.decoderURLBASE64(newfilename), "utf-8");
		Path source = Paths.get(filepath, newfilename);
		boolean pathExists = Files.exists(source, new LinkOption[] { LinkOption.NOFOLLOW_LINKS });
		if (pathExists) {
			throw new IOException("文件已存在！");
		} else {
			try {
				Files.createDirectory(source);
			} catch (IOException e) {
				logger.error(e);
				return false;
			}
			return true;
		}
	}
}

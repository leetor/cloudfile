package cloudfile.control;
import java.io.File;
import java.nio.file.*;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
public class docp {

    // 要存放的文件夹位置
    private final String SAVE_PATH = "C:\\Users\\sym\\sb\\backups";
    // 没用到的数据
    // private static String filePath;
    // 复制的文件总数
    private static int fileNum = 0;
    public Path gg=null;
    public String getFilePath() {
        File file = gg.toFile();
        String path = file.toString().replaceAll("\\\\", "/");
        return path;
    }
    public void copy() throws IOException {
        File file = new File(getFilePath());
        System.out.println("###"+file);
        fileWritePath(file);
    }

    // 1、获取该文件或文件夹下所有文件路径
    // 2、如果是文件则备份到指定位置
    // 3、如果是文件夹则循环遍历
    public void fileWritePath(File source) throws IOException {
        // 如果是文件夹迭代
        if (source.isDirectory()) {
            if (source.listFiles() != null && source.listFiles().length != 0)
                for (File file : source.listFiles())
                    fileWritePath(file);
        }
        // System.out.println(source);
        // 如果是文件，且不是该文件自身
        if (source.isFile()
                && source.toString().indexOf("FileSave.class") == -1) {
            System.out.println(source);
            copyFile(source);
            // 对备份的文件数进行统计
            fileNum++;
        }
    }

    // 字节流文件复制
    public void copy_sy4(String oldFile, String newFile) throws IOException {
        FileInputStream infile = new FileInputStream(oldFile);
        FileOutputStream outfile = new FileOutputStream(newFile, true);
        byte[] bb = new byte[1024];
        int lenth = 0;
        while ((lenth = infile.read(bb)) != -1) {
            outfile.write(bb, 0, lenth);
        }
        outfile.flush();
        outfile.close();
        infile.close();
    }

    //对指定的文件进行复制操作
    public void copyFile(File source) throws IOException {
        String newPath = SAVE_PATH
                + source.getAbsolutePath().toString().replaceAll("\\\\", "/")
                .replaceAll(getFilePath(), "");
        newPath = newPath.replaceAll("/", "\\\\");
        //获取新文件所在路径

        System.out.println(newPath);
        File oldFile = new File(newPath);
        // 如果文件存在
        if (oldFile.exists()) {
            fileNewName(oldFile);
            System.out.println("文件存在");
            copy_sy4(source.toString(), oldFile.toString());
        }
        // 如果文件不存在
        else {
            // 先创建该文件所在的文件夹
            File file = new File(oldFile.toString().substring(0,
                    (newPath.lastIndexOf("\\"))));
            // 同时创建多层文件夹
            file.mkdirs();
            System.out.println("文件不存在");
            // 进行文件复制操作
            copy_sy4(source.toString(), oldFile.toString());
        }
    }
    public void fileNewName(File file) {
        String fileSuffix = new SimpleDateFormat("HH-mm-ss").format(new Date());
        file.renameTo(new File(file.toString() + "~" + fileSuffix));
    }
    public void docopy(Path p) throws IOException {

        docp f = new docp();
        f.gg=p;
        f.copy();
        System.out.println("copynum=" + fileNum);
    }
}

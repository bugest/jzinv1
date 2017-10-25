package nc.bs.impl.ftp;

public class UploadStatus {
	/** 远程服务器相应目录创建失败 */
	public static final int CREATE_DIRECTORY_FAIL = 0;
	
	/** 远程服务器闯将目录成功 */
	public static final int CREATE_DIRECTORY_SUCCESS = 1;
	
	/** 上传新文件成功 */
	public static final int UPLOAD_NEW_FILE_SUCCESS = 2;
	
	/** 上传新文件失败 */
	public static final int UPLOAD_NEW_FILE_FAILED = 3;
	
	/** 文件已经存在 */
	public static final int FILE_EXITS = 4;
	
	/** 远程文件大于本地文件 */
	public static final int REMOTE_BIGGER_LOCAL = 5;
	
	/** 断点续传成功 */
	public static final int UPLOAD_FROM_BREAK_SUCCESS = 6;
	
	/** 断点续传失败 */
	public static final int UPLOAD_FROM_BREAK_FAILED = 7;
	
	/** 删除远程文件失败 */
	public static final int DELETE_REMOTE_FAILD = 8;
}

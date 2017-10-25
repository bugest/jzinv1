package nc.bs.impl.ftp;

public class DownloadStatus {
	/** 远程文件不存在 */
	public static final int REMOTE_FILE_NOEXIST = 0;
	
	/** 本地文件大于远程文件 */
	public static final int LOCAL_BIGGER_REMOTE = 1;
	
	/** 断点下载文件成功 */
	public static final int DOWNLOAD_FROM_BREAK_SUCCESS = 2;
	
	/** 断点下载文件失败 */
	public static final int DOWNLOAD_FROM_BREAK_FAILED = 3;
	
	/** 全新下载文件成功 */
	public static final int DOWNLOAD_NEW_SUCCESS = 4;
	
	/** 全新下载文件失败 */
	public static final int DOWNLOAD_NEW_FAILED = 5;

}

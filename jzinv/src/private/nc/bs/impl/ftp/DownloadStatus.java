package nc.bs.impl.ftp;

public class DownloadStatus {
	/** Զ���ļ������� */
	public static final int REMOTE_FILE_NOEXIST = 0;
	
	/** �����ļ�����Զ���ļ� */
	public static final int LOCAL_BIGGER_REMOTE = 1;
	
	/** �ϵ������ļ��ɹ� */
	public static final int DOWNLOAD_FROM_BREAK_SUCCESS = 2;
	
	/** �ϵ������ļ�ʧ�� */
	public static final int DOWNLOAD_FROM_BREAK_FAILED = 3;
	
	/** ȫ�������ļ��ɹ� */
	public static final int DOWNLOAD_NEW_SUCCESS = 4;
	
	/** ȫ�������ļ�ʧ�� */
	public static final int DOWNLOAD_NEW_FAILED = 5;

}

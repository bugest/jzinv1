package nc.bs.impl.ftp;

public class UploadStatus {
	/** Զ�̷�������ӦĿ¼����ʧ�� */
	public static final int CREATE_DIRECTORY_FAIL = 0;
	
	/** Զ�̷���������Ŀ¼�ɹ� */
	public static final int CREATE_DIRECTORY_SUCCESS = 1;
	
	/** �ϴ����ļ��ɹ� */
	public static final int UPLOAD_NEW_FILE_SUCCESS = 2;
	
	/** �ϴ����ļ�ʧ�� */
	public static final int UPLOAD_NEW_FILE_FAILED = 3;
	
	/** �ļ��Ѿ����� */
	public static final int FILE_EXITS = 4;
	
	/** Զ���ļ����ڱ����ļ� */
	public static final int REMOTE_BIGGER_LOCAL = 5;
	
	/** �ϵ������ɹ� */
	public static final int UPLOAD_FROM_BREAK_SUCCESS = 6;
	
	/** �ϵ�����ʧ�� */
	public static final int UPLOAD_FROM_BREAK_FAILED = 7;
	
	/** ɾ��Զ���ļ�ʧ�� */
	public static final int DELETE_REMOTE_FAILD = 8;
}

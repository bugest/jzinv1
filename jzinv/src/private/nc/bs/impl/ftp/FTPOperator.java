package nc.bs.impl.ftp;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

public class FTPOperator {
	private static final String FILE_ENCODE_GBK = "GBK";
	public FTPClient ftpClient = new FTPClient();

	/**
	 * ���ӵ�FTP������
	 * 
	 * @param hostname
	 *            ������
	 * @param port
	 *            �˿�
	 * @param username
	 *            �û���
	 * @param password
	 *            ����
	 * @return �Ƿ����ӳɹ�
	 * @throws IOException
	 */
	public boolean connect(String hostname, int port, String username,
			String password) throws Exception {

		try {
			ftpClient.connect(hostname, port);
		} catch (Exception e) {
			throw new Exception("��½�쳣�����������˿�");
		}
		ftpClient.setControlEncoding(FILE_ENCODE_GBK);
		if (FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
			if (ftpClient.login(username, password)) {
				return true;
			} else
				throw new Exception("��½�쳣�����������˺�");
		} else {
			throw new Exception("��½�쳣");
		}

	}

	/**
	 * �Ͽ���Զ�̷�����������
	 * 
	 * @throws IOException
	 */
	public void disconnect() throws IOException {
		if (ftpClient.isConnected()) {
			ftpClient.disconnect();
		}
	}

	public int upload(String content, String remote) throws IOException {
		// ����PassiveMode����
		ftpClient.enterLocalPassiveMode();
		// �����Զ��������ķ�ʽ����
		ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

		ftpClient.setControlEncoding(FILE_ENCODE_GBK);

		// ��Զ��Ŀ¼�Ĵ���
		String remoteFileName = remote;
		if (remote.contains("/")) {
			remoteFileName = remote.substring(remote.lastIndexOf("/") + 1);
			// ����������Զ��Ŀ¼�ṹ������ʧ��ֱ�ӷ���
			if (CreateDirecroty(remote, ftpClient) == UploadStatus.CREATE_DIRECTORY_FAIL) {
				return UploadStatus.CREATE_DIRECTORY_FAIL;
			}
		}

		int status = uploadFile(remoteFileName, content);

		return status;
	}

	public FTPResult download(String remote) throws IOException {
		String remoteFolder = remote.indexOf(File.separator) >= 0 ? remote.substring(0, remote.indexOf(File.separator)) : remote;
		String remoteFile = remote.indexOf(File.separator) >= 0 ? remote.substring( remote.indexOf(File.separator) + 1 ) : remote;
		
		// ���ñ���ģʽ
		ftpClient.enterLocalPassiveMode();

		// �����Զ����Ʒ�ʽ����
		ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
		
		// �����ļ�·��
		ftpClient.changeWorkingDirectory(remoteFolder);
		
		FTPResult result = new FTPResult();

		// ���Զ���ļ��Ƿ����
		FTPFile[] files = ftpClient.listFiles( getGBKStr(remoteFile) );
		if (files.length != 1) {
			result.setStatus(DownloadStatus.REMOTE_FILE_NOEXIST);

			return result;
		}

		InputStream in = ftpClient.retrieveFileStream( getGBKStr(remoteFile) );

		BufferedReader br = new BufferedReader( new InputStreamReader(in) );
		
		try {
			StringBuffer sb = new StringBuffer();
			String line = br.readLine();
			while ( line != null ) {
				sb.append(line);
				line = br.readLine();
			}
			
			result.setFileContent(sb.toString());
		} catch (Exception e) {
			
		} finally {
			if (in != null) {
				in.close();
			}
		}
		
		boolean upNewStatus = ftpClient.completePendingCommand();
		result.setStatus(upNewStatus ? DownloadStatus.DOWNLOAD_NEW_SUCCESS
				: DownloadStatus.DOWNLOAD_NEW_FAILED);

		return result;
	}

	public int uploadFile(String remoteFile, String content) throws IOException {
		ftpClient.deleteFile(remoteFile);
		
		OutputStream out = ftpClient.appendFileStream( getGBKStr(remoteFile) );

		out.write(content.getBytes());
		out.flush();
		out.close();

		int status;
		boolean result = ftpClient.completePendingCommand();
		status = result ? UploadStatus.UPLOAD_NEW_FILE_SUCCESS
				: UploadStatus.UPLOAD_NEW_FILE_FAILED;

		return status;
	}

	/**
	 * �ݹ鴴��Զ�̷�����Ŀ¼
	 * 
	 * @param remote
	 *            Զ�̷������ļ�����·��
	 * @param ftpClient
	 *            FTPClient����
	 * @return Ŀ¼�����Ƿ�ɹ�
	 * @throws IOException
	 */
	public int CreateDirecroty(String remote, FTPClient ftpClient)
			throws IOException {
		int status = UploadStatus.CREATE_DIRECTORY_SUCCESS;
		String directory = remote.substring(0, remote.lastIndexOf("/") + 1);
		if ( !directory.equalsIgnoreCase("/") && !ftpClient.changeWorkingDirectory( getGBKStr(directory) ) ) {
			// ���Զ��Ŀ¼�����ڣ���ݹ鴴��Զ�̷�����Ŀ¼
			int start = 0;
			int end = 0;
			if (directory.startsWith("/")) {
				start = 1;
			} else {
				start = 0;
			}
			end = directory.indexOf("/", start);
			while (true) {
				String subDirectory = getGBKStr(remote.substring(start, end));
				if (!ftpClient.changeWorkingDirectory(subDirectory)) {
					if (ftpClient.makeDirectory(subDirectory)) {
						ftpClient.changeWorkingDirectory(subDirectory);
					} else {
						System.out.println("����Ŀ¼ʧ��");
						return UploadStatus.CREATE_DIRECTORY_FAIL;
					}
				}

				start = end + 1;
				end = directory.indexOf("/", start);

				// �������Ŀ¼�Ƿ񴴽����
				if (end <= start) {
					break;
				}
			}
		}
		return status;
	}
	
	private String getGBKStr(String str) throws UnsupportedEncodingException {
		return new String( str.getBytes(FILE_ENCODE_GBK), FILE_ENCODE_GBK );
	}

	public static void main(String[] args) throws Exception {
		FTPOperator ftp = new FTPOperator();
		String content = "receive info for upload tax goverment";

		// ��ַ���칫��192.168.8.86������125.35.5.152
		// �˿ڣ�21
		// �û�/���룺jzhj/jzhj
		System.out.println("begin open ftp connection");
		ftp.connect("192.168.8.86", 21, "jzhj", "jzhj");
		System.out.println("end open ftp connection");

		System.out.println("begin ftp upload");
		ftp.upload(content, "test/abc.txt");
		System.out.println("end ftp upload");

		System.out.println("begin ftp download");
		FTPResult result = ftp.download("test/abc.txt");
		System.out.println("ftp file context: " + result.getFileContent());
		System.out.println("end ftp download");

		System.out.println("begin ftp close");
		ftp.disconnect();
		System.out.println("end ftp close");
	}
}

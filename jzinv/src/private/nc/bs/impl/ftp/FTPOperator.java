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
	 * 连接到FTP服务器
	 * 
	 * @param hostname
	 *            主机名
	 * @param port
	 *            端口
	 * @param username
	 *            用户名
	 * @param password
	 *            密码
	 * @return 是否连接成功
	 * @throws IOException
	 */
	public boolean connect(String hostname, int port, String username,
			String password) throws Exception {

		try {
			ftpClient.connect(hostname, port);
		} catch (Exception e) {
			throw new Exception("登陆异常，请检查主机端口");
		}
		ftpClient.setControlEncoding(FILE_ENCODE_GBK);
		if (FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
			if (ftpClient.login(username, password)) {
				return true;
			} else
				throw new Exception("登陆异常，请检查密码账号");
		} else {
			throw new Exception("登陆异常");
		}

	}

	/**
	 * 断开与远程服务器的连接
	 * 
	 * @throws IOException
	 */
	public void disconnect() throws IOException {
		if (ftpClient.isConnected()) {
			ftpClient.disconnect();
		}
	}

	public int upload(String content, String remote) throws IOException {
		// 设置PassiveMode传输
		ftpClient.enterLocalPassiveMode();
		// 设置以二进制流的方式传输
		ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

		ftpClient.setControlEncoding(FILE_ENCODE_GBK);

		// 对远程目录的处理
		String remoteFileName = remote;
		if (remote.contains("/")) {
			remoteFileName = remote.substring(remote.lastIndexOf("/") + 1);
			// 创建服务器远程目录结构，创建失败直接返回
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
		
		// 设置被动模式
		ftpClient.enterLocalPassiveMode();

		// 设置以二进制方式传输
		ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
		
		// 设置文件路径
		ftpClient.changeWorkingDirectory(remoteFolder);
		
		FTPResult result = new FTPResult();

		// 检查远程文件是否存在
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
	 * 递归创建远程服务器目录
	 * 
	 * @param remote
	 *            远程服务器文件绝对路径
	 * @param ftpClient
	 *            FTPClient对象
	 * @return 目录创建是否成功
	 * @throws IOException
	 */
	public int CreateDirecroty(String remote, FTPClient ftpClient)
			throws IOException {
		int status = UploadStatus.CREATE_DIRECTORY_SUCCESS;
		String directory = remote.substring(0, remote.lastIndexOf("/") + 1);
		if ( !directory.equalsIgnoreCase("/") && !ftpClient.changeWorkingDirectory( getGBKStr(directory) ) ) {
			// 如果远程目录不存在，则递归创建远程服务器目录
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
						System.out.println("创建目录失败");
						return UploadStatus.CREATE_DIRECTORY_FAIL;
					}
				}

				start = end + 1;
				end = directory.indexOf("/", start);

				// 检查所有目录是否创建完毕
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

		// 地址：办公网192.168.8.86，外网125.35.5.152
		// 端口：21
		// 用户/密码：jzhj/jzhj
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

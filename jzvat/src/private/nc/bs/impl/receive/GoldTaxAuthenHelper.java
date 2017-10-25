package nc.bs.impl.receive;

import java.io.File;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nc.bs.framework.common.NCLocator;
import nc.bs.impl.ftp.DownloadStatus;
import nc.bs.impl.ftp.FTPOperator;
import nc.bs.impl.ftp.FTPResult;
import nc.itf.jzinv.pub.IJZPMPubBusi;
import nc.itf.jzinv.pub.model.AuthenPropModel;
import nc.vo.jzinv.vatgoldtax.AggVatGoldtaxVO;
import nc.vo.jzinv.vatgoldtax.VatGoldtaxBVO;
import nc.vo.jzinv.vatgoldtax.VatGoldtaxVO;
import nc.vo.pub.CircularlyAccessibleValueObject;
import nc.vo.pub.lang.UFDouble;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

public class GoldTaxAuthenHelper {
	private static final AuthenPropModel goldTaxModel = NCLocator.getInstance().lookup(IJZPMPubBusi.class).getAuthenPropModel();
	
	private static FTPOperator ftp = new FTPOperator();
	
	public static void uploadFile(String content, String nsr, String fileName) throws Exception {
		ftp.connect(goldTaxModel.getIp(), Integer.parseInt(goldTaxModel.getPort()), goldTaxModel.getFtpuser(), goldTaxModel.getFtppsw());

		String uploadFolder = goldTaxModel.getNsrUploadFolderMap().get(nsr).getUploadFolder();
		ftp.upload(content, uploadFolder + File.separator + fileName);

		ftp.disconnect();
	}
	
	/**
	 * download result
	 * @param fileName
	 * @return
	 */
	public static Map<String, AggVatGoldtaxVO> downloadFiles(List<AggVatGoldtaxVO> aggGoldTaxList) throws Exception {
		ftp.connect(goldTaxModel.getIp(), Integer.parseInt(goldTaxModel.getPort()), goldTaxModel.getFtpuser(), goldTaxModel.getFtppsw());

		Map<String, AggVatGoldtaxVO> result = new HashMap<String, AggVatGoldtaxVO>();
		String content = null;
		VatGoldtaxVO goldTaxVO = null;
		for ( AggVatGoldtaxVO aggGoldTaxVo : aggGoldTaxList ) {
			goldTaxVO = (VatGoldtaxVO)aggGoldTaxVo.getParentVO();
			content = downloadFile( goldTaxVO );
			if (content != null && content.length() > 0) {
				parseResult(aggGoldTaxVo, content);
				result.put( goldTaxVO.getVauthname(), aggGoldTaxVo );
			}
		}
		
		ftp.disconnect();
		
		return result;
	}
	
	private static void parseResult(AggVatGoldtaxVO aggGoldTaxVo, String content) throws DocumentException {
		Map<Integer, VatGoldtaxBVO> bvoMap = new HashMap<Integer, VatGoldtaxBVO>();
		VatGoldtaxBVO bvo = null;
		for ( CircularlyAccessibleValueObject objGoldTaxBVo : aggGoldTaxVo.getChildrenVO() ) {
			bvo = (VatGoldtaxBVO) objGoldTaxBVo;
			bvoMap.put(bvo.getRowid(), bvo);
		}
		
		Document doc = DocumentHelper.parseText(content);
		Element body = doc.getRootElement();
		
		Element data = body.element("data");
		List<Element> rows = data.elements("row");
		
		for ( Element row : rows ) {
			int rowId = Integer.parseInt(row.attribute("id").getValue());

			bvoMap.get(rowId).setJg( row.element("jg").getText() );
			bvoMap.get(rowId).setRq( row.element("rq").getText() );
		}
		
	}
	
	private static String downloadFile(VatGoldtaxVO goldTaxVo) throws Exception {
		String downloadFolder = goldTaxModel.getNsrUploadFolderMap().get(goldTaxVo.getNsr()).getDownloadFolder();
		FTPResult result = ftp.download(downloadFolder + File.separator + goldTaxVo.getVresultname());
		
		String content = null;
		if (result.getStatus() == DownloadStatus.DOWNLOAD_NEW_SUCCESS) {
			content = result.getFileContent();
		}
		
		return content;
	}
	
	public static Element createDataElement(CircularlyAccessibleValueObject[] goldTaxBVos) {
		Element data = DocumentHelper.createElement("data");
		Element row = null;
		
		VatGoldtaxBVO bvo = null;
		for (int rowIdx = 0; rowIdx < goldTaxBVos.length; rowIdx++) {
			bvo = (VatGoldtaxBVO) goldTaxBVos[rowIdx];
			row = createaRowElement(bvo);
			
			data.add(row);
		}
		
		return data;
	}

	private static Element createaRowElement(VatGoldtaxBVO bvo) {
		Element row = DocumentHelper.createElement("row");
		row.addAttribute("id", bvo.getRowid().toString());
		
		Element dm = DocumentHelper.createElement("dm");
		dm.setText(bvo.getDm());
		row.add(dm);

		Element hm = DocumentHelper.createElement("hm");
		hm.setText(bvo.getHm());
		row.add(hm);

		Element gf = DocumentHelper.createElement("gf");
		gf.setText(bvo.getGf());
		row.add(gf);

		Element xf = DocumentHelper.createElement("xf");
		xf.setText(bvo.getXf());
		row.add(xf);

		Element kr = DocumentHelper.createElement("kr");
		kr.setText(bvo.getKr());
		row.add(kr);

		Element je = DocumentHelper.createElement("je");
		je.setText( getDouToStr(bvo.getJe()) );
		row.add(je);

		Element se = DocumentHelper.createElement("se");
		se.setText( getDouToStr(bvo.getSe()) );
		row.add(se);

		Element mw = DocumentHelper.createElement("mw");
		mw.setText(bvo.getMw());
		row.add(mw);

		Element sy = DocumentHelper.createElement("sy");
		sy.setText(bvo.getSy());
		row.add(sy);

		Element bb = DocumentHelper.createElement("bb");
		bb.setText(bvo.getBb());
		row.add(bb);
		
		return row;
	}
	
	private static String getDouToStr(UFDouble val) {
		DecimalFormat df = new DecimalFormat("0.00");
		return df.format(val.getDouble());
	}

	public static Element createHeadElement(VatGoldtaxVO goldTaxVo, int rowCnt, String user, String psw) {
		Element head = DocumentHelper.createElement("head");
		
		Element code = DocumentHelper.createElement("code");
		code.setText(goldTaxVo.getCode());
		head.add(code);
		
		Element title = DocumentHelper.createElement("title");
		title.setText(goldTaxVo.getTitle());
		head.add(title);
		
		Element nsr = DocumentHelper.createElement("nsr");
		nsr.setText(goldTaxVo.getNsr());
		head.add(nsr);
		
		Element qymc = DocumentHelper.createElement("qymc");
		qymc.setText(goldTaxVo.getQymc());
		head.add(qymc);
		
		Element scrq = DocumentHelper.createElement("scrq");
		scrq.setText(goldTaxVo.getScrq());
		head.add(scrq);
		
		Element rows = DocumentHelper.createElement("rows");
		rows.setText(goldTaxVo.getRows().toString());
		head.add(rows);
		
		Element czymc = DocumentHelper.createElement("czymc");
		czymc.setText(user);
		head.add(czymc);
		
		Element czypass = DocumentHelper.createElement("czypass");
		czypass.setText(psw);
		head.add(czypass);
		
		return head;
	}

}

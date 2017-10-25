package nc.ui.jzinv.receive;

import java.awt.Container;

import nc.ui.pub.pf.IinitQueryData2;
import nc.ui.querytemplate.normalpanel.INormalQueryPanel;
import nc.ui.trade.query.HYQueryConditionDLG;
import nc.vo.querytemplate.TemplateInfo;
/**
 * 收票参照发票信息采集查询框
 * @author mayyc
 *
 */
public class ClientRefQueryDlg extends HYQueryConditionDLG implements
		IinitQueryData2 {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4698308298965299258L;

	public ClientRefQueryDlg(Container parent, INormalQueryPanel normalPnl) {
		super(parent, normalPnl);
	}

	public ClientRefQueryDlg(Container parent, TemplateInfo ti) {
		super(parent, ti);
	}

	public void initData(String pkCorp, String operator, String funNode,
			String businessType, String currentBillType, String sourceBilltype,
			String nodeKey, Object userObj) throws Exception {

	}

}
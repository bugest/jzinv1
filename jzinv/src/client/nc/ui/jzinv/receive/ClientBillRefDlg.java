package nc.ui.jzinv.receive;

import java.awt.Container;
import java.util.ArrayList;
import java.util.List;

import nc.ui.jzinv.pub.refbill.JZBillSourceDLG;
import nc.ui.pub.beans.MessageDialog;
import nc.ui.pub.bill.BillModel;
import nc.vo.jzinv.inv0503.ReceiveCollVO;
import nc.vo.trade.pub.HYBillVO;
/**
 * 收票参照发票信息采集
 * @author mayyc
 *
 */
public class ClientBillRefDlg extends JZBillSourceDLG {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8725869203478579751L;
	
	public ClientBillRefDlg(String pkField, String pkCorp, String operator,
			String funNode, String queryWhere, String billType,
			String businessType, String templateId, String currentBillType,
			Container parent) {
		super(pkField, pkCorp, operator, funNode, queryWhere, billType, businessType,
				templateId, currentBillType, parent);
	}
	@Override
	protected boolean isHeadCanMultiSelect() {
		return false;
	}
	@Override
	protected boolean isBodyShow() {
		return false;
	}
	protected boolean isBodyCanSelected() {
		return false;
	}
	@Override
	public void onOk() {
		int headRowCount = getbillListPanel().getHeadBillModel().getRowCount();
		List<Integer> selectRows = getSelectHeadRows(headRowCount);		
	
		if (null != selectRows && selectRows.size() > 0) {
			HYBillVO[] selectVOS = new HYBillVO[selectRows.size()];
			ReceiveCollVO selectVO = null;
			for(int i = 0 ;i < selectRows.size(); i++){
				selectVO = (ReceiveCollVO) getbillListPanel().getHeadBillModel().getBodyValueRowVO(selectRows.get(i), m_billHeadVo);	
				selectVOS[i]  =  new HYBillVO();
				selectVOS[i].setParentVO(selectVO);
			} 
			retBillVos = selectVOS;
		}
		this.closeOK();
		
	}
	/**
	 * 获得表头选中行
	 * @param headRowCount
	 * @return
	 */
	private List<Integer> getSelectHeadRows(int headRowCount){
		List<Integer> selectRows = new ArrayList<Integer>();
		if (headRowCount > 0) {
			boolean isHeadSelState = getbillListPanel().getHeadBillModel()
					.isHasSelectRow();
			if (!isHeadSelState) {
				MessageDialog.showErrorDlg(this, "错误", "未选中任何表头数据！");
				return null;
			}
			for (int i = 0; i < headRowCount; i++) {
				if (getbillListPanel().getHeadBillModel().getRowState(i) == BillModel.SELECTED) {
					selectRows.add(i);
				}
			}
		}
		else{
			MessageDialog.showErrorDlg(this, "错误", "表头无数据！");
			return null;
		}
		return selectRows;
	}

	@Override
	public String getHeadCondition() {
		StringBuffer condition = new StringBuffer();
		String pk_corp = getPkCorp();
		condition.append(" (bisregister='N' or bisregister is null) and isnull(dr,0)=0 " );
		condition.append(" and (pk_corp = '" + pk_corp + "') ");
		return condition.toString();
	}
	
}
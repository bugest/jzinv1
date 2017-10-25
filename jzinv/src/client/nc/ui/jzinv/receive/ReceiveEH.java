package nc.ui.jzinv.receive;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import nc.bs.framework.common.NCLocator;
import nc.bs.logging.Logger;
import nc.itf.jzinv.receive.IReceiveService;
import nc.ui.jzinv.pub.eventhandler.ManageEventHandler;
import nc.ui.jzinv.receive.action.ReceAddAction;
import nc.ui.jzinv.receive.action.ReceCancelAction;
import nc.ui.jzinv.receive.action.ReceEditAction;
import nc.ui.jzinv.receive.action.ReceRedAction;
import nc.ui.jzinv.receive.action.ReceSaveAction;
import nc.ui.pub.ButtonObject;
import nc.ui.pub.pf.PfUtilClient;
import nc.ui.trade.base.IBillOperate;
import nc.ui.trade.bill.RefBillTypeChangeEvent;
import nc.ui.trade.controller.IControllerBase;
import nc.ui.trade.manage.BillManageUI;
import nc.vo.jzinv.inv0503.ReceiveCollVO;
import nc.vo.jzinv.pub.IJzinvBillType;
import nc.vo.jzinv.pub.IJzinvButton;
import nc.vo.jzinv.pub.utils.SafeCompute;
import nc.vo.jzinv.receive.AggReceiveVO;
import nc.vo.jzinv.receive.ReceiveDetailVO;
import nc.vo.jzinv.receive.ReceiveVO;
import nc.vo.pub.AggregatedValueObject;
import nc.vo.pub.BusinessException;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDouble;

public class ReceiveEH extends ManageEventHandler{

	public ReceiveEH(BillManageUI billUI, IControllerBase control) {
		super(billUI, control);
	}
	
	@Override
	public boolean isEnableMultiTablePRT(){
		return true;
	}
	
    @Override
	public void onBoAdd(ButtonObject bo) throws Exception{
    	setHeadItemEditable(true);
		super.onBoAdd(bo);
		new ReceAddAction((BillManageUI) getBillManageUI()).doAction();
	}
	@Override
	protected void onBoElse(int intBtn) throws Exception {
		super.onBoElse(intBtn);
		switch (intBtn) {
		case IJzinvButton.REFADDCOLLBTN:
			onBillRef(intBtn);
			break;
		case IJzinvButton.INV_RED:
			onRed();
		default:
			break;
		}
	}

	@SuppressWarnings("deprecation")
	private void onRed() throws Exception {
		ButtonObject bo = new ButtonObject("期初");
		super.onBoAdd(bo);
		new ReceRedAction(getBillManageUI()).doAction();
	}

	public void onBillRef(int intBtn) throws Exception {
		ButtonObject btn = getButtonManager().getButton(intBtn);
		btn.setTag(getBillUI().getRefBillType() + ":");
		onBoBusiTypeAdd(btn, null);

		btn.setTag(String.valueOf(intBtn));
		if (!PfUtilClient.isCloseOK()) {
			return;
		}else{
			getClientUI().setDefaultData();
			setHeadItemEditable(false);
		}
	}
	private ReceiveUI getClientUI() {
		return (ReceiveUI) getBillUI();
	}
	@Override
	protected boolean isDataChange() {
		return false;
	}
	private void setHeadItemEditable(boolean edit){
		String[] fields = new String[]{ReceiveVO.NINVMNY, ReceiveVO.NINVTAXMNY, ReceiveVO.NTAXMNY, ReceiveVO.NTAXRATE};
		for(String field : fields){
			getBillManageUI().getBillCardPanel().getHeadItem(field).setEdit(edit);
			if(field.equals(ReceiveVO.NTAXMNY)){
				continue;
			}
			getBillManageUI().getBillCardPanel().getHeadItem(field).setNull(true);
		}
	}
	private final void onBoBusiTypeAdd(ButtonObject bo, String sourceBillId)
			throws Exception {
				getBusiDelegator().childButtonClicked(bo, _getCorp().getPrimaryKey(),
						getBillUI()._getModuleCode(), _getOperator(),
						getUIController().getBillType(), getBillUI(),
						getBillUI().getUserObject(), sourceBillId);
				if (nc.ui.pub.pf.PfUtilClient.makeFlag) {
					// 设置单据状态
					getBillUI().setCardUIState();
					// 新增
					getBillUI().setBillOperate(IBillOperate.OP_ADD);
				} else {
					if (PfUtilClient.isCloseOK()) {
						if (getBillBusiListener() != null) {
							String tmpString = bo.getTag();
							int findIndex = tmpString.indexOf(":");
							String newtype = tmpString.substring(0, findIndex);
							RefBillTypeChangeEvent e = new RefBillTypeChangeEvent(this,
									null, newtype);
							getBillBusiListener().refBillTypeChange(e);
						}
						if (isDataChange())
							setRefData(PfUtilClient.getRetVos());
						else
							setRefData(PfUtilClient.getRetOldVos());

					} 
				}
	}
	@Override
	protected AggregatedValueObject refVOChange(AggregatedValueObject[] vos)
			throws Exception {
		if(null == vos || vos.length == 0){
			return null;
		}
		ReceiveCollVO collVO = (ReceiveCollVO) vos[0].getParentVO();
		ReceiveVO receHVO = new ReceiveVO();
		receHVO.setPk_project(collVO.getPk_project());//项目
		receHVO.setPk_projectbase(collVO.getPk_projectbase());//项目
		receHVO.setPk_supplier(collVO.getPk_supplier());//供应商(销售方)
		receHVO.setPk_supplierbase(collVO.getPk_supplier_base());//供应商
		receHVO.setPk_contract(collVO.getPk_contract());//合同
		receHVO.setDopendate(collVO.getDopendate());//开票日期
		receHVO.setDtodate(collVO.getDopendate());//票到日期
		receHVO.setVinvno(collVO.getVinvno());//发票号
		receHVO.setVinvcode(collVO.getVinvcode());//发票代码
		receHVO.setVtaxpayername(collVO.getVtaxpayername());//购方名称
		receHVO.setVtaxpayernumber(collVO.getVtaxpayerno());//购方纳税人识别号
		receHVO.setVtaxpayerphone(collVO.getVtaxpayeraddress());//购方地址电话
		receHVO.setVbankaccount(collVO.getVtaxpayeraccount());//购方开户银行及银行帐号
		receHVO.setVtaxsuppliernumber(collVO.getVtaxsupplieno());//销售方纳税人识别号
		receHVO.setVsupplierphone(collVO.getVtaxsupplieaddr());////销售方地址电话
		receHVO.setVsupbankaccount(collVO.getVtaxsupplieaccount());//销售方开户行及账号
		receHVO.setNinvmny(collVO.getNinvmny());//发票金额(无税)
		receHVO.setNinvtaxmny(collVO.getNinvtaxmny());//发票金额(含税)
		receHVO.setNoriginvmny(collVO.getNinvmny());//发票金额(原币无税)
		receHVO.setNoriginvtaxmny(collVO.getNinvtaxmny());//发票金额(原币含税)
		receHVO.setNtaxmny(collVO.getNtaxmny());//税额
		//税率
		UFDouble ntaxrate = SafeCompute.div(collVO.getNtaxmny(), collVO.getNinvmny());
		receHVO.setNtaxrate(SafeCompute.multiply(new UFDouble(100), ntaxrate));
		receHVO.setVlastbillid(collVO.getPk_receive_coll());//发票信息采集主键
		//供应商类型
		receHVO.setIsupplytype(collVO.getIsupplytype());
//		项目主键	vsecretcode
		receHVO.setVsecret(collVO.getVsecretcode());//密文
		
		AggReceiveVO aggVo = new AggReceiveVO();
		
		String pk_corp = getClientUI().getPk_corp();
		//IJzinvBillType.JZINV_RECEIVE_MT 应该取值各个收票的单据类型
		boolean bisupload = nc.vo.jzinv.param.InvParamTool.isToGLByBilltype(pk_corp, getClientUI().getUIControl().getBillType());
		receHVO.setBisupload(bisupload ? UFBoolean.TRUE : UFBoolean.FALSE);
		aggVo.setParentVO(receHVO);
		setBodyVO(aggVo);
		return aggVo;
	}
	
	private void setBodyVO(AggReceiveVO aggVo) {
		ReceiveVO hvo = (ReceiveVO) aggVo.getParentVO();
		ReceiveDetailVO detailVO = new ReceiveDetailVO();
		detailVO.setNtaxmny(hvo.getNtaxmny());
		detailVO.setNtaxrate(hvo.getNtaxrate());
		detailVO.setNthrecemny(hvo.getNinvmny());
		detailVO.setNthrecetaxmny(hvo.getNinvtaxmny());
		aggVo.setTableVO(ReceiveDetailVO.TABCODE, new ReceiveDetailVO[]{detailVO});
	}

	@Override
	protected void onBoLineAdd() throws Exception {
		super.onBoLineAdd();
		if(ReceiveDetailVO.TABCODE.equals(this.getBillCardPanelWrapper().getBillCardPanel().getCurrentBodyTableCode())) {
			UFDouble ntaxRate = new UFDouble((String)this.getClientUI().getBillCardPanel().getHeadItem(ReceiveVO.NTAXRATE).getValueObject());
			int row = this.getClientUI().getBillCardPanel().getBillModel(ReceiveDetailVO.TABCODE).getRowCount();
			this.getClientUI().getBillCardPanel().setBodyValueAt(ntaxRate, row-1, ReceiveVO.NTAXRATE);
			String pk_project = (String)this.getClientUI().getBillCardPanel().getHeadItem(ReceiveVO.PK_PROJECT).getValueObject();
			String pk_projectbase = (String)this.getClientUI().getBillCardPanel().getHeadItem(ReceiveVO.PK_PROJECTBASE).getValueObject();
			
			this.getClientUI().getBillCardPanel().setBodyValueAt(pk_project, row-1, ReceiveVO.PK_PROJECT, ReceiveDetailVO.TABCODE);
			this.getClientUI().getBillCardPanel().setBodyValueAt(pk_projectbase, row-1, ReceiveVO.PK_PROJECTBASE, ReceiveDetailVO.TABCODE);
			this.getClientUI().getBillCardPanel().getBillModel(ReceiveDetailVO.TABCODE).execLoadFormula();
		}
		
	}
	@Override
	protected void onBoLineDel() throws Exception {
		super.onBoLineDel();
		
	}
	
	@Override
	protected void onBoLinePaste() throws Exception {
		super.onBoLinePaste();
		
	}
	
	@Override
	protected void onBoLinePasteToTail() throws Exception {
		super.onBoLinePasteToTail();
	
	}
	
	@Override
	protected void onBoSave() throws Exception {
		new ReceSaveAction((BillManageUI) getBillManageUI()).doAction();
		super.onBoSave();
	}
	
	@Override
	protected void onBoCancel() throws Exception {
		super.onBoCancel();
        new ReceCancelAction((BillManageUI) getBillManageUI()).doAction();
	}
	
	@Override
	protected void onBoEdit() throws Exception {
		String vlastbillid = (String) getClientUI().getBillCardPanel().getHeadItem(ReceiveVO.VLASTBILLID).getValueObject();
		if(!StringUtils.isEmpty(vlastbillid)){
			setHeadItemEditable(false);
		}
		else{
			setHeadItemEditable(true);
		}
		//linan 20171025 根据单据状况设置税金拆分单可编辑性
		setTaxSplitFields();
		super.onBoEdit();
	    new ReceEditAction((BillManageUI) getBillManageUI()).doAction();
	}
	
	@Override
	public void onBoAudit() throws Exception {
		super.onBoAudit();
		onBoRefresh();
	}
	
	@Override
	protected void onBoCancelAudit() throws Exception {
		super.onBoCancelAudit();
		onBoRefresh();
	}
	
	/** 
	* @Title: setTaxSplitFields 
	* @Description: 设置拆分税金相关字段
	* @param     
	* @return void    
	* @throws 
	*/
	private void setTaxSplitFields() {
		String vinvcode = (String)this.getClientUI().getBillCardPanel().getHeadItem(ReceiveVO.VINVCODE).getValueObject();
		String vinvno = (String)this.getClientUI().getBillCardPanel().getHeadItem(ReceiveVO.VINVNO).getValueObject();
		//如果发票号和发票code有一个为空就不能编辑，这种情况应该不会发生，但是还是判断下，以防万一
		if(vinvcode == null || vinvcode.trim().equals("") || vinvno == null || vinvno.trim().equals("")) {
			//设置非必填
			this.getClientUI().getBillCardPanel().getHeadItem(ReceiveVO.NTOTALINVOICEAMOUNTMNY).setNull(false);
			this.getClientUI().getBillCardPanel().getHeadItem(ReceiveVO.NTOTALINVOICEAMOUNTTAXMNY).setNull(false);
			this.getClientUI().getBillCardPanel().getHeadItem(ReceiveVO.NTOTALINVOICETAX).setNull(false);
			//设置字段的编辑性 都不可编辑
			this.getClientUI().getBillCardPanel().getHeadItem(ReceiveVO.NTOTALINVOICEAMOUNTMNY).setEdit(false);
			this.getClientUI().getBillCardPanel().getHeadItem(ReceiveVO.NTOTALINVOICEAMOUNTTAXMNY).setEdit(false);
			this.getClientUI().getBillCardPanel().getHeadItem(ReceiveVO.NTOTALINVOICETAX).setEdit(false);	
			this.getClientUI().getBillCardPanel().getHeadItem(ReceiveVO.BISSPLIT).setEdit(false);
			return;
		}
		UFBoolean bissplit = new UFBoolean(this.getClientUI().getBillCardPanel().getHeadItem(ReceiveVO.BISSPLIT).getValueObject().toString());
		String pk_receive = (String)this.getClientUI().getBillCardPanel().getHeadItem(ReceiveVO.PK_RECEIVE).getValueObject();
		//只要发票部位空，这个字段就可以填写
		this.getClientUI().getBillCardPanel().getHeadItem(ReceiveVO.BISSPLIT).setEdit(true);
		//如果发票号no code都有，就分情况设置可编辑性
		//如果选中时，都是可以编辑的，并且是非空的
		if(bissplit.equals(UFBoolean.TRUE)) {	
			try {
				List<ReceiveVO> receiveVOList = NCLocator.getInstance()
						.lookup(IReceiveService.class)
						.querySplitHeadVOsByCond(vinvcode, vinvno, pk_receive);
				//判断是不是对于本发票只有一个单据
				if(receiveVOList == null || receiveVOList.isEmpty()) {
					this.getClientUI().getBillCardPanel().getHeadItem(ReceiveVO.NTOTALINVOICEAMOUNTMNY).setEdit(bissplit.booleanValue());
					this.getClientUI().getBillCardPanel().getHeadItem(ReceiveVO.NTOTALINVOICEAMOUNTTAXMNY).setEdit(bissplit.booleanValue());
					this.getClientUI().getBillCardPanel().getHeadItem(ReceiveVO.NTOTALINVOICETAX).setEdit(bissplit.booleanValue());	
					this.getClientUI().getBillCardPanel().getHeadItem(ReceiveVO.BISSPLIT).setEdit(bissplit.booleanValue());
					this.getClientUI().getBillCardPanel().getHeadItem(ReceiveVO.NTOTALINVOICEAMOUNTTAXMNY).setNull(bissplit.booleanValue());
					this.getClientUI().getBillCardPanel().getHeadItem(ReceiveVO.NTOTALINVOICETAX).setNull(bissplit.booleanValue());
				} else {
					this.getClientUI().getBillCardPanel().getHeadItem(ReceiveVO.NTOTALINVOICEAMOUNTMNY).setEdit(false);
					this.getClientUI().getBillCardPanel().getHeadItem(ReceiveVO.NTOTALINVOICEAMOUNTTAXMNY).setEdit(false);
					this.getClientUI().getBillCardPanel().getHeadItem(ReceiveVO.NTOTALINVOICETAX).setEdit(false);	
					this.getClientUI().getBillCardPanel().getHeadItem(ReceiveVO.NTOTALINVOICEAMOUNTMNY).setNull(bissplit.booleanValue());
					this.getClientUI().getBillCardPanel().getHeadItem(ReceiveVO.NTOTALINVOICEAMOUNTTAXMNY).setNull(bissplit.booleanValue());
					this.getClientUI().getBillCardPanel().getHeadItem(ReceiveVO.NTOTALINVOICETAX).setNull(bissplit.booleanValue());					
				}
			} catch (BusinessException e) {
				Logger.error("查询发票拆分情况报错！", e);
				getClientUI().showErrorMessage("查询发票拆分情况报错!");
			}

		} else {
			this.getClientUI().getBillCardPanel().getHeadItem(ReceiveVO.NTOTALINVOICEAMOUNTMNY).setEdit(bissplit.booleanValue());
			this.getClientUI().getBillCardPanel().getHeadItem(ReceiveVO.NTOTALINVOICEAMOUNTTAXMNY).setEdit(bissplit.booleanValue());
			this.getClientUI().getBillCardPanel().getHeadItem(ReceiveVO.NTOTALINVOICETAX).setEdit(bissplit.booleanValue());	
			this.getClientUI().getBillCardPanel().getHeadItem(ReceiveVO.NTOTALINVOICEAMOUNTMNY).setNull(bissplit.booleanValue());
			this.getClientUI().getBillCardPanel().getHeadItem(ReceiveVO.NTOTALINVOICEAMOUNTTAXMNY).setNull(bissplit.booleanValue());
			this.getClientUI().getBillCardPanel().getHeadItem(ReceiveVO.NTOTALINVOICETAX).setNull(bissplit.booleanValue());			
		}
		

	}
}
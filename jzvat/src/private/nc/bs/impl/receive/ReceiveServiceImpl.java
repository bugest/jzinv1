package nc.bs.impl.receive;

import java.io.IOException;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import nc.bs.dao.BaseDAO;
import nc.bs.dao.DAOException;
import nc.bs.framework.common.InvocationInfoProxy;
import nc.bs.framework.common.NCLocator;
import nc.bs.framework.exception.ComponentException;
import nc.bs.logging.Logger;
import nc.bs.pub.pflock.ILockData;
import nc.bs.pub.pflock.PfBusinessLock;
import nc.bs.trade.comsave.BillSave;
import nc.itf.jzinv.pub.IJZPMStatefulInvoker;
import nc.itf.jzinv.receive.IReceiveService;
import nc.itf.uif.pub.IUifService;
import nc.jdbc.framework.processor.BeanListProcessor;
import nc.uif.pub.exception.UifException;
import nc.vo.jzinv.goldtax.GoldTaxConsts;
import nc.vo.jzinv.goldtax.IAuthStatus;
import nc.vo.jzinv.inv0510.OpenHVO;
import nc.vo.jzinv.invpub.AuthenConditionVO;
import nc.vo.jzinv.invpub.InvConsts;
import nc.vo.jzinv.invpub.tool.IndexCodeGenerator;
import nc.vo.jzinv.invpub.tool.TimeLoader;
import nc.vo.jzinv.pub.BillDateGetter;
import nc.vo.jzinv.pub.JZINVProxy;
import nc.vo.jzinv.pub.JzInvocationParam;
import nc.vo.jzinv.pub.utils.SafeCompute;
import nc.vo.jzinv.receive.AggReceiveVO;
import nc.vo.jzinv.receive.ReceiveBVO;
import nc.vo.jzinv.receive.ReceiveDetailVO;
import nc.vo.jzinv.receive.ReceiveVO;
import nc.vo.jzinv.vatgoldtax.AggVatGoldtaxVO;
import nc.vo.jzinv.vatgoldtax.VatGoldtaxBVO;
import nc.vo.jzinv.vatgoldtax.VatGoldtaxVO;
import nc.vo.jzinv.vatinvoice.AggVatGtInvoiceVO;
import nc.vo.jzinv.vatinvoice.VatGtInvoiceVO;
import nc.vo.logging.Debug;
import nc.vo.pub.BusinessException;
import nc.vo.pub.CircularlyAccessibleValueObject;
import nc.vo.pub.SuperVO;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDate;
import nc.vo.pub.lang.UFDateTime;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

public class ReceiveServiceImpl implements IReceiveService {

	private static IUifService service;

	/**
	 * @return Returns the service.
	 */
	public static IUifService getService() throws UifException {
		if (service == null) {
			try {
				service = (IUifService) NCLocator.getInstance().lookup(IUifService.class.getName());
			} catch (ComponentException e) {
				Debug.debug("can't find " + e.getComponentName(), e);
				throw new UifException();
			}
		}
		return service;

	}

	public AggReceiveVO queryAggVoByPK(String pk_Receive) throws Exception {
		String[] userObj = { AggReceiveVO.class.getName(), ReceiveVO.class.getName(), ReceiveBVO.class.getName(),
				ReceiveDetailVO.class.getName() };
		AggReceiveVO aggVO = (AggReceiveVO) getService().queryBillVOByPrimaryKey(userObj, pk_Receive);
		return aggVO;
	}

	public void updateSelectVOs(ReceiveVO[] hvos) throws Exception {

		final String[] lockPks = getLockPKs(hvos);
		if (lockPks == null || lockPks.length == 0)
			return;
		//��̬��PK����---����
		new PfBusinessLock().lock(new ILockData() {
			public String[] getLockPks() throws BusinessException {
				return lockPks;
			}

		}, null);
		//1.��������Ƿ�Ϸ�
		checkInValidData(hvos);
		//2.�������ݿ�
		IJZPMStatefulInvoker invoker = JZINVProxy.getStatefulInvoker();
		JzInvocationParam chkParam = new JzInvocationParam("nc.bs.trade.business.HYSuperDMO", "updateArray",
				new Class[] { SuperVO[].class, String[].class }, new Object[] {
						hvos,
						new String[] { ReceiveVO.PK_AUTHENPSN, ReceiveVO.DAUTHENDATE, ReceiveVO.VTAXPERIOD,
								ReceiveVO.BISAUTHEN, ReceiveVO.IAUTHSTATUS } });
		invoker.invoke(chkParam);
	}

	/**
	 * ��������Ƿ�Ϸ�
	 * @param hvos
	 * @throws Exception
	 */
	private void checkInValidData(ReceiveVO[] hvos) throws Exception {
		checkBillTsChange(hvos);
	}

	private String[] getLockPKs(ReceiveVO[] hvos) {
		List<String> pkList = new ArrayList<String>();
		for (ReceiveVO hvo : hvos) {
			String pk_primary = hvo.getPrimaryKey();
			pkList.add(pk_primary);
		}
		return pkList.size() > 0 ? pkList.toArray(new String[0]) : null;
	}

	/**
	 * �ж�ts�Ƿ����ı�
	 * @param hvos
	 * @throws Exception
	 */
	private void checkBillTsChange(ReceiveVO[] hvos) throws Exception {
		Map<String, UFDateTime> cur_billStatusMap = new HashMap<String, UFDateTime>();
		for (ReceiveVO hvo : hvos) {
			String pk_primary = hvo.getPrimaryKey();
			UFDateTime oldts = hvo.getTs();
			cur_billStatusMap.put(pk_primary, oldts);
		}
		StringBuffer pks = new StringBuffer();
		for (String pk_primary : cur_billStatusMap.keySet()) {
			pks.append("'" + pk_primary + "',");
		}
		//����pkList��ѯ���ݿ������µ���
		StringBuffer strWhere = new StringBuffer();
		strWhere.append(" pk_receive in (" + pks.deleteCharAt(pks.lastIndexOf(",")) + ")");
		//SuperVO[] newHVOs = HYPubBO_Client.queryByCondition(ReceiveVO.class, strWhere.toString());
		ReceiveVO[] newHVOs = (ReceiveVO[]) getService().queryByCondition(ReceiveVO.class, strWhere.toString());
		//new_billStatusMap�洢���ݿ��е����µ���״̬
		Map<String, UFDateTime> new_billStatusMap = new HashMap<String, UFDateTime>();
		if (null != newHVOs && newHVOs.length > 0) {
			for (ReceiveVO newHVO : newHVOs) {
				String pk_primary = newHVO.getPrimaryKey();
				UFDateTime newts = newHVO.getTs();
				new_billStatusMap.put(pk_primary, newts);
			}
		}
		if (new_billStatusMap.size() > 0) {
			boolean billTsChanged = false;
			for (Entry<String, UFDateTime> entry : new_billStatusMap.entrySet()) {
				String pk_primary = entry.getKey();
				UFDateTime newTS = entry.getValue();
				if (cur_billStatusMap.containsKey(pk_primary)) {
					UFDateTime curTs = cur_billStatusMap.get(pk_primary);
					if (newTS.compareTo(curTs) != 0) {
						billTsChanged = true;
						break;
					}
				}
			}
			if (billTsChanged) {
				throw new BusinessException("�����ѱ��޸�, ��ˢ������!");
			}
		}
	}

	@SuppressWarnings("unchecked")
	public List<ReceiveVO> queryHeadVOsByCond(AuthenConditionVO conditionVO) throws Exception {
		String pk_corp = conditionVO.getPk_corp();
		String pk_finance = conditionVO.getPk_finance();
		String pk_projectbase = conditionVO.getPk_projectbase();
		//		UFDate denddate = conditionVO.getDenddate();
		//		UFDouble ntotaltaxmny = conditionVO.getNtotaltaxmny();

		StringBuffer whereSql = new StringBuffer();
		whereSql.append(" select r.* from jzinv_receive r  ");//join jzinv_vat_projtaxset p on r.pk_project = p.pk_project
		whereSql.append(" where r.pk_finance = '").append(pk_finance).append("'");
		whereSql.append(" and (r.bisauthen = 'N' or r.bisauthen is null) ");
		whereSql.append(" and (r.bistransfertax = 'N' or r.bistransfertax is null) ");
		whereSql.append(" and (r.iauthstatus = 0) ");
		whereSql.append(" and r.vbillstatus = 1 ");
		whereSql.append(" and (r.bisdrawback = 'N' or r.bisdrawback is null) ");//�����ǳ�����˰�ķ�Ʊ add by mayyc
		whereSql.append(" and ((r.pk_project not in "
				+ "(SELECT pk_project FROM jzinv_vat_projtaxset where dr = 0 and itaxtype = 1)) "
				+ "or (r.pk_project is null)) ");//�����Ǽ��׼�����ʽ�ķ�Ʊ�Ľ���˰� add by sujbc
		if (pk_corp != null && pk_corp.length() > 0) {
			whereSql.append(" and r.pk_corp = '").append(pk_corp).append("'");
		}
		if (pk_projectbase != null && pk_projectbase.length() > 0) {
			whereSql.append(" and r.pk_projectbase = '").append(pk_projectbase).append("'");
		}
		List<ReceiveVO> result = (List<ReceiveVO>) new BaseDAO().executeQuery(whereSql.toString(),
				new BeanListProcessor(ReceiveVO.class));
		ReceiveVO[] vos = result.toArray(new ReceiveVO[0]);//(ReceiveVO[])getService().queryByCondition(ReceiveVO.class, whereSql.toString());
		List<ReceiveVO> voList = new ArrayList<ReceiveVO>();
		voList.addAll(Arrays.asList(vos));

		Collections.sort(voList, new Comparator<ReceiveVO>() {
			public int compare(ReceiveVO o1, ReceiveVO o2) {
				//add by songlx ���NullPoint
				if (o1.getDenddate() != null && o2.getDenddate() != null) {

					if (o1.getDenddate().before(o2.getDenddate())) {
						return -1;
					} else if (o1.getDenddate().equals(o2.getDenddate())) {
						return SafeCompute.compare(o1.getNtaxmny(), o2.getNtaxmny());
					}
				}
				return 1;
			}
		});

		return voList;

	}

	public void goldTaxAuthen(List<AggReceiveVO> aggVos, String pk_corp, String pk_user, String code, String password)
			throws Exception {
		// 1��group the receive list
		Map<String, List<AggReceiveVO>> receiveMap = new HashMap<String, List<AggReceiveVO>>();
		Map<String, String[]> nsrInfoMap = new HashMap<String, String[]>();
		receiveGroup(aggVos, receiveMap, nsrInfoMap);
		String[] nsrInfo = null;
		for (String nsrcode : receiveMap.keySet()) {
			nsrInfo = nsrInfoMap.get(nsrcode);
			oneGoldTaxAuth(receiveMap.get(nsrcode), pk_corp, nsrInfo[0], nsrInfo[1], pk_user, code, password);
		}
	}

	private void receiveGroup(List<AggReceiveVO> aggVos, Map<String, List<AggReceiveVO>> receiveMap,
			Map<String, String[]> nsrInfoMap) {
		ReceiveVO receiveVo = null;
		String[] nsrInfo = null;
		for (AggReceiveVO aggVo : aggVos) {
			receiveVo = (ReceiveVO) aggVo.getParentVO();
			nsrInfo = new String[2];
			nsrInfo[0] = receiveVo.getVtaxpayernumber();
			nsrInfo[1] = receiveVo.getVtaxpayername();

			if (!receiveMap.containsKey(nsrInfo[0])) {
				receiveMap.put(nsrInfo[0], new ArrayList<AggReceiveVO>());
				nsrInfoMap.put(nsrInfo[0], nsrInfo);
			}

			receiveMap.get(nsrInfo[0]).add(aggVo);
		}
	}

	private void oneGoldTaxAuth(List<AggReceiveVO> aggVos, String pk_corp, String nsr, String nsrName, String pk_user,
			String code, String password) throws BusinessException {
		try {
			// 1�� create gold tax data struct
			AggVatGoldtaxVO aggGoldTaxVo = createGoldTax(aggVos, pk_corp, nsr, nsrName, pk_user);

			// 2��create gold tax xml data
			String xmlContent = createGoldTaxContent(aggGoldTaxVo, code, password);

			// 3��upload gold tax xml file
			VatGoldtaxVO goldTaxVo = (VatGoldtaxVO) aggGoldTaxVo.getParentVO();
			GoldTaxAuthenHelper.uploadFile(xmlContent, nsr, goldTaxVo.getVauthname());

			// 4��save gold tax data struct
			new BillSave().saveBill(aggGoldTaxVo);

			// 5�� write back receive data
			List<ReceiveVO> receiveVoList = fillReceiveVoInfo(aggVos, goldTaxVo.getVauthname());
			new BaseDAO().updateVOArray(receiveVoList.toArray(new ReceiveVO[0]), new String[] {
					ReceiveVO.BISTRANSFERTAX, ReceiveVO.IAUTHSTATUS, ReceiveVO.VUPLOADFILENAME });

		} catch (IOException e) {
			throw new BusinessException(e);
		} catch (Exception e) {
			throw new BusinessException(e);
		}

	}

	private List<ReceiveVO> fillReceiveVoInfo(List<AggReceiveVO> aggVos, String fileName) {
		List<ReceiveVO> receiveList = new ArrayList<ReceiveVO>();
		ReceiveVO receiveVo = null;
		for (AggReceiveVO aggVo : aggVos) {
			receiveVo = (ReceiveVO) aggVo.getParentVO();
			receiveVo.setVuploadfilename(fileName);
			receiveVo.setIauthstatus(IAuthStatus.AUTHEN_STATUS_AUTHING);
			receiveVo.setBistransfertax(UFBoolean.TRUE);

			receiveList.add(receiveVo);
		}

		return receiveList;
	}

	private String createGoldTaxContent(AggVatGoldtaxVO aggGoldTaxVo, String user, String psw) throws IOException {
		Element body = DocumentHelper.createElement("body");

		VatGoldtaxVO goldTaxVo = (VatGoldtaxVO) aggGoldTaxVo.getParentVO();
		CircularlyAccessibleValueObject[] goldTaxBVos = aggGoldTaxVo.getChildrenVO();

		// fill authen head info
		Element head = GoldTaxAuthenHelper.createHeadElement(goldTaxVo, goldTaxBVos.length, user, psw);
		body.add(head);

		// fill authen receive info
		Element data = GoldTaxAuthenHelper.createDataElement(goldTaxBVos);
		body.add(data);

		// get xml info
		OutputFormat f = new OutputFormat();
		f.setEncoding("GBK");

		Document doc = DocumentHelper.createDocument(body);
		StringWriter out = new StringWriter();
		XMLWriter w = new XMLWriter(out, f);
		w.write(doc);
		w.flush();

		return out.toString();
	}

	private AggVatGoldtaxVO createGoldTax(List<AggReceiveVO> aggVos, String pk_corp, String nsrCode, String nsrName,
			String pk_user) throws BusinessException {
		AggVatGoldtaxVO aggGoldTaxVo = new AggVatGoldtaxVO();

		String timeSnap = TimeLoader.getInstance().getDateTimeStr();
		// create gold tax head info
		VatGoldtaxVO goldTaxVo = new VatGoldtaxVO();
		goldTaxVo.setPk_corp(pk_corp);
		goldTaxVo.setVauthname(GoldTaxConsts.GOLD_TAX_UPLOAD_CODE + nsrCode + timeSnap + ".xml");
		goldTaxVo.setVresultname(GoldTaxConsts.GOLD_TAX_DOWNLOAD_CODE + nsrCode + timeSnap + ".xml");
		goldTaxVo.setPk_authoperator(pk_user);
		goldTaxVo.setAuthts(BillDateGetter.getServerTime());
		goldTaxVo.setIsget(UFBoolean.FALSE);

		goldTaxVo.setCode(GoldTaxConsts.GOLD_TAX_UPLOAD_CODE);
		goldTaxVo.setTitle(GoldTaxConsts.GOLD_TAX_UPLOAD_TITLE);
		goldTaxVo.setNsr(nsrCode);
		goldTaxVo.setQymc(nsrName);
		goldTaxVo.setScrq(timeSnap.substring(0, 8));
		goldTaxVo.setRows(aggVos.size());

		aggGoldTaxVo.setParentVO(goldTaxVo);

		List<VatGoldtaxBVO> goldTaxBVoList = new ArrayList<VatGoldtaxBVO>();
		VatGoldtaxBVO goldTaxBVo = null;
		ReceiveVO receiveVo = null;
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");

		// create gold tax data info
		int rowId = 1;
		for (AggReceiveVO aggVo : aggVos) {
			receiveVo = (ReceiveVO) aggVo.getParentVO();

			goldTaxBVo = new VatGoldtaxBVO();
			goldTaxBVo.setRowid(rowId);
			rowId++;
			goldTaxBVo.setPk_receive(receiveVo.getPk_receive());
			goldTaxBVo.setGf(receiveVo.getVtaxpayernumber());
			goldTaxBVo.setXf(receiveVo.getVtaxsuppliernumber());
			goldTaxBVo.setDm(receiveVo.getVinvcode());
			goldTaxBVo.setHm(receiveVo.getVinvno());
			goldTaxBVo.setKr(receiveVo.getDopendate() == null ? "" : df.format(receiveVo.getDopendate().toDate()));
			goldTaxBVo.setJe(receiveVo.getNinvmny());
			goldTaxBVo.setSe(receiveVo.getNtaxmny());
			goldTaxBVo.setMw(receiveVo.getVsecret());
			goldTaxBVo.setSy(IndexCodeGenerator.getInstance().nextId());
			goldTaxBVo.setBb(GoldTaxConsts.GOLD_TAX_ENCODE_VERSION);

			goldTaxBVoList.add(goldTaxBVo);
		}

		aggGoldTaxVo.setChildrenVO(goldTaxBVoList.toArray(new VatGoldtaxBVO[0]));

		return aggGoldTaxVo;
	}

	@SuppressWarnings("unchecked")
	public void fetchGoldTaxAuthResult(String pk_corp, String pk_operator) throws BusinessException {
		BaseDAO dao = new BaseDAO();

		try {
			// 1. search authen list that didn't get result
			Collection<VatGoldtaxVO> goldTaxColl = dao.retrieveByClause(VatGoldtaxVO.class,
					" isnull(dr, 0) = 0 and isnull(isget, 'N') = 'N' ");
			Collection<VatGoldtaxBVO> goldTaxBColl = dao
					.retrieveByClause(VatGoldtaxBVO.class,
							" pk_goldtax in (select pk_goldtax from jzinv_vat_goldtax where isnull(dr, 0) = 0 and isnull(isget, 'N') = 'N') ");
			Map<String, List<VatGoldtaxBVO>> goldTaxBVoMap = new HashMap<String, List<VatGoldtaxBVO>>();
			for (VatGoldtaxBVO goldTaxBVo : goldTaxBColl) {
				if (!goldTaxBVoMap.containsKey(goldTaxBVo.getPk_goldtax())) {
					goldTaxBVoMap.put(goldTaxBVo.getPk_goldtax(), new ArrayList<VatGoldtaxBVO>());
				}

				goldTaxBVoMap.get(goldTaxBVo.getPk_goldtax()).add(goldTaxBVo);
			}

			List<AggVatGoldtaxVO> aggGoldTaxList = new ArrayList<AggVatGoldtaxVO>();
			AggVatGoldtaxVO aggGoldTaxVo = null;
			for (VatGoldtaxVO goldTax : goldTaxColl) {
				aggGoldTaxVo = new AggVatGoldtaxVO();
				aggGoldTaxVo.setParentVO(goldTax);
				aggGoldTaxVo.setChildrenVO(goldTaxBVoMap.get(goldTax.getPk_goldtax()).toArray(new VatGoldtaxBVO[0]));

				aggGoldTaxList.add(aggGoldTaxVo);
			}

			// 2. download result files
			Map<String, AggVatGoldtaxVO> result = GoldTaxAuthenHelper.downloadFiles(aggGoldTaxList);

			if (result.size() == 0) {
				return;
			}

			// 3. search receive by result
			StringBuffer fileNameSql = new StringBuffer();
			for (String fileName : result.keySet()) {
				fileNameSql.append(", '").append(fileName).append("'");
			}
			Collection<ReceiveVO> receiveVoColl = dao.retrieveByClause(ReceiveVO.class,
					" isnull(dr, 0) = 0 and vuploadfilename in (" + fileNameSql.substring(1) + ") ");
			List<ReceiveVO> receiveVoList = new ArrayList<ReceiveVO>();
			for (ReceiveVO receiveVo : receiveVoColl) {
				receiveVoList.add(receiveVo);
			}

			// 5. update receive's
			for (String fileName : result.keySet()) {
				updResultIntoReceive(dao, result.get(fileName), receiveVoList, pk_operator);
			}
		} catch (DAOException e) {
			Logger.error(e);
			throw new BusinessException(e);
		} catch (Exception e) {
			Logger.error(e);
			throw new BusinessException(e);
		}
	}

	private void updResultIntoReceive(BaseDAO dao, AggVatGoldtaxVO aggGoldTaxVO, List<ReceiveVO> receiveVoList,
			String pk_operator) throws DAOException {
		VatGoldtaxVO goldTaxVo = (VatGoldtaxVO) aggGoldTaxVO.getParentVO();
		Map<String, VatGoldtaxBVO> receiveRstMap = new HashMap<String, VatGoldtaxBVO>();
		VatGoldtaxBVO bvo = null;
		for (CircularlyAccessibleValueObject bvoObj : aggGoldTaxVO.getChildrenVO()) {
			bvo = (VatGoldtaxBVO) bvoObj;
			receiveRstMap.put(bvo.getDm() + bvo.getHm(), bvo);
		}

		List<ReceiveVO> updReceiveList = new ArrayList<ReceiveVO>();
		for (ReceiveVO receiveVo : receiveVoList) {
			if (goldTaxVo.getVauthname().equals(receiveVo.getVuploadfilename())) {
				if (receiveRstMap.containsKey(receiveVo.getVinvcode() + receiveVo.getVinvno())) {
					bvo = receiveRstMap.get(receiveVo.getVinvcode() + receiveVo.getVinvno());
					if ("0".equals(bvo.getJg())) {
						receiveVo.setIauthstatus(IAuthStatus.AUTHEN_STATUS_AUTH_PASS);
					} else {
						receiveVo.setIauthstatus(IAuthStatus.AUTHEN_STATUS_UN_PASS);
					}

					updReceiveList.add(receiveVo);
				}
			}
		}

		dao.updateVOArray(updReceiveList.toArray(new ReceiveVO[0]), new String[] { ReceiveVO.IAUTHSTATUS });

		goldTaxVo.setPk_resultoperator(pk_operator);
		goldTaxVo.setIsget(UFBoolean.TRUE);
		goldTaxVo.setResultts(BillDateGetter.getServerTime());
		dao.updateVO(goldTaxVo, new String[] { VatGoldtaxVO.ISGET, VatGoldtaxVO.PK_RESULTOPERATOR,
				VatGoldtaxVO.RESULTTS });

		updateAuthenInfo(dao, updReceiveList);
	}

	/**
	 * ������֤��Ϣ
	 * @author mayyc
	 * @param dao
	 * @param updReceiveList
	 * @throws BusinessException
	 */
	private void updateAuthenInfo(BaseDAO dao, List<ReceiveVO> updReceiveList) throws DAOException {
		//Ŀǰ����˰�ڵĴ����Ȱ���ǰ�ڼ䴦��
		String curDate = InvocationInfoProxy.getInstance().getDate().toString();
		String vtaxperiod = curDate.substring(0, 7);
		for (ReceiveVO receiveVO : updReceiveList) {
			Integer iauthstatus = receiveVO.getIauthstatus();
			if (iauthstatus == InvConsts.AUTHSTATUS_AUTHSUCESS) {
				//���¡��Ƿ���֤��ΪY����֤�ˡ���֤����
				receiveVO.setBisauthen(UFBoolean.TRUE);
				receiveVO.setPk_authenpsn(InvocationInfoProxy.getInstance().getUserCode());
				receiveVO.setDauthendate(new UFDate(InvocationInfoProxy.getInstance().getDate()));
				receiveVO.setVtaxperiod(vtaxperiod);
			}
		}
		dao.updateVOArray(updReceiveList.toArray(new ReceiveVO[0]), new String[] { ReceiveVO.PK_AUTHENPSN,
				ReceiveVO.DAUTHENDATE, ReceiveVO.BISAUTHEN, ReceiveVO.VTAXPERIOD });
	}

	// ================ api about output ==============================================================================
	/**
	 * create gold tax invoice info and write back invoice open info
	 */
	public void writeBackInvoiceAndOpenInfo(AggVatGtInvoiceVO aggInvoiceVo, OpenHVO headVo) throws BusinessException {
		// insert gold tax invoice info
		new BillSave().saveBill(aggInvoiceVo);

		// update open invoice info
		new BaseDAO().updateVO(headVo, new String[] { OpenHVO.VINVCODE, OpenHVO.VINVNO });

	}

	/**
	 * write back invoice info when cancel invoice
	 */
	@SuppressWarnings("unchecked")
	public void writeBackCancelInvoice(OpenHVO openHeadVo) throws BusinessException {
		// find gold tax invoice info by open invoice info
		BaseDAO dao = new BaseDAO();
		Collection<VatGtInvoiceVO> invoiceColl = dao.retrieveByClause(VatGtInvoiceVO.class,
				" isnull(dr, 0) = 0 and pk_src_receive = '" + openHeadVo.getPk_open() + "'");

		if (invoiceColl.size() == 0) {
			VatGtInvoiceVO invoice = invoiceColl.iterator().next();
			invoice.setIscancel(UFBoolean.TRUE);

			// update gold tax invoice info
			dao.updateVO(invoice, new String[] { VatGtInvoiceVO.ISCANCEL });
		}

		// update open invoice info
		openHeadVo.setBisabolish(UFBoolean.TRUE);
		dao.updateVO(openHeadVo, new String[] { OpenHVO.BISABOLISH });
	}

	@SuppressWarnings("unchecked")
	public void writeBackUploadInfo(List<OpenHVO> headVoList) throws BusinessException {
		BaseDAO dao = new BaseDAO();

		// find gold tax invoice info by open invoice info
		Map<String, OpenHVO> openHeadMap = new HashMap<String, OpenHVO>();
		StringBuffer whereSql = new StringBuffer();
		for (OpenHVO headVo : headVoList) {
			openHeadMap.put(headVo.getPk_open(), headVo);

			whereSql.append(",'").append(headVo.getPk_open()).append("'");
		}

		Collection<VatGtInvoiceVO> invoiceColl = dao.retrieveByClause(VatGtInvoiceVO.class,
				" isnull(dr, 0) = 0 and pk_src_receive in (" + whereSql.substring(1) + ")");
		OpenHVO headVo = null;
		List<VatGtInvoiceVO> invoiceList = new ArrayList<VatGtInvoiceVO>();
		for (VatGtInvoiceVO invoice : invoiceColl) {
			headVo = openHeadMap.get(invoice.getPk_src_receive());
			invoice.setUploadFlag(headVo.getIinvstatus());

			invoiceList.add(invoice);
		}

		dao.updateVOArray(invoiceList.toArray(new VatGtInvoiceVO[0]), new String[] { VatGtInvoiceVO.UPLOADFLAG });
		dao.updateVOArray(headVoList.toArray(new OpenHVO[0]), new String[] { OpenHVO.IINVSTATUS });

	}

	/* (non-Javadoc)
	 * @see nc.itf.jzinv.receive.IReceiveService#querySplitHeadVOsByCond(java.lang.String, java.lang.String, java.lang.String)
	 */
	public List<ReceiveVO> querySplitHeadVOsByCond(String vinvcode,
			String vinvno, String pk_receive) throws BusinessException {
		StringBuffer sqlStr = new StringBuffer();
		sqlStr.append(" select * from jzinv_receive r ");
		sqlStr.append(" where VINVCODE = '" + vinvcode + "'");
		sqlStr.append(" and VINVNO = '" + vinvno + "'");
		if(pk_receive == null || "".equals(pk_receive.trim())) {
			sqlStr.append(" and PK_RECEIVE is not null ");
		} else {
			sqlStr.append(" and PK_RECEIVE <> '" + pk_receive + "'");
		}	
		sqlStr.append(" and dr = 0 ");
		List<ReceiveVO> result = (List<ReceiveVO>) new BaseDAO().executeQuery(sqlStr.toString(),
				new BeanListProcessor(ReceiveVO.class));
		ReceiveVO[] vos = result.toArray(new ReceiveVO[0]);//(ReceiveVO[])getService().queryByCondition(ReceiveVO.class, whereSql.toString());
		List<ReceiveVO> voList = new ArrayList<ReceiveVO>();
		voList.addAll(Arrays.asList(vos));
/*		Collections.sort(voList, new Comparator<ReceiveVO>() {
			public int compare(ReceiveVO o1, ReceiveVO o2) {
				//add by songlx ���NullPoint
				if (o1.getDenddate() != null && o2.getDenddate() != null) {

					if (o1.getDenddate().before(o2.getDenddate())) {
						return -1;
					} else if (o1.getDenddate().equals(o2.getDenddate())) {
						return SafeCompute.compare(o1.getNtaxmny(), o2.getNtaxmny());
					}
				}
				return 1;
			}
		});*/
		return voList;
	}

}

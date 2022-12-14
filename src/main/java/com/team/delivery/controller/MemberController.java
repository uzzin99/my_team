package com.team.delivery.controller;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Random;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;


import com.team.delivery.DTO.*;
import com.team.delivery.MailUtils;
import com.team.delivery.TempKey;
import com.team.delivery.mappers.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@Slf4j
@RequiredArgsConstructor
public class MemberController {

	private final JavaMailSender javaMailSender;
	private final iMember ime;
	private final iMenuStore ims;
  	private final iBooking ibo;
	private final iStore store;
	private final iCart ica;

	@Value("${spring.mail.username}")
	private String fromMail;
	@Value("${part.upload.path}")
	private String uploadfolder;

		@RequestMapping("/reviewDel")
		@ResponseBody
		public String ReviewDel(@RequestParam("delSe") int delse){
			log.info("리뷰삭제번호={}",delse);
			ica.reviewRemove(delse);
			int checkDel = store.reviewDel(delse);
			return Integer.toString(checkDel);
		}


		@RequestMapping("/signUp/payment")
		public String paymentDetails(HttpServletRequest request, Model model){

			HttpSession session=request.getSession();

			model.addAttribute("userType",session.getAttribute("userType"));
			model.addAttribute("mname",session.getAttribute("mName"));

// eunji
			String mid = (String)session.getAttribute("userid");
			ArrayList<reviewDTO> rlist = store.myReviewList(mid);
			model.addAttribute("rlist",rlist);
// yoojin
			ArrayList<bookingDTO>reservationlist = ibo.reservationlist(mid);
			model.addAttribute("list",reservationlist);
// jeon
			ArrayList<orderDTO> orderList = ica.selOrder(mid);
			ArrayList<oDetailDTO> detailList = ica.selDetail();
			System.out.println(orderList);
			model.addAttribute("orderList",orderList);
			model.addAttribute("detailList",detailList);

			return "member/paymentDetails";
		}

		@RequestMapping("/signUp/dvListup")
		public String dvList(@RequestParam("se") int se, Model model) {
			mDTO selList = ime.seList(se);
			model.addAttribute("list",selList);
			return "member/dvList";
		}
		@RequestMapping("/signUp/dvList")
		public String dvList(Model model) {
			model.addAttribute("list","");
			return "member/dvList";
		}
		
		@RequestMapping("/signUp/delSe")
		public String delSe(@RequestParam("se") int se) {
			ime.delSe(se);
			return "redirect:/signUp/deliveryUp";
		}
		
		@RequestMapping("/signUp/upDelivery")
		public String upDelivery(@RequestParam("newname") String newname,
				@RequestParam("newmobile") String newmobile,
				@RequestParam("postcode") String postcode,
				@RequestParam("address") String address,
				@RequestParam("detailAddress") String detailaddress,
				@RequestParam("extraAddress") String extraaddress,
				@RequestParam("checked") String checked,
				@RequestParam("se") int se,
				HttpServletRequest request) {
			HttpSession session=request.getSession();
			String mId=(String)session.getAttribute("userid");

			if(checked.equals("Y")) {
				ime.upDeliveryList(postcode, address, detailaddress, extraaddress, newname,newmobile, se);
				ime.upDelivery(postcode, address, detailaddress, extraaddress,mId);
			}else {
				ime.upDeliveryList(postcode, address, detailaddress, extraaddress, newname,newmobile, se);
			}
			
			return "redirect:/signUp/dvList";
		}
		
		@RequestMapping("/signUp/addDelivery")
		public String addDelivery(@RequestParam("newname") String newname,
				@RequestParam("newmobile") String newmobile,
				@RequestParam("postcode") String postcode,
				@RequestParam("address") String address,
				@RequestParam("detailAddress") String detailaddress,
				@RequestParam("extraAddress") String extraaddress,
				@RequestParam("checked") String checked,
				HttpServletRequest request) {
			HttpSession session=request.getSession();
			String mId=(String)session.getAttribute("userid");
			

			if(checked.equals("Y")) {
				ime.addDelivery(mId, address, postcode, detailaddress, extraaddress, newname, newmobile);
				ime.upDelivery(postcode, address, detailaddress, extraaddress,mId);
			}else {
				ime.addDelivery(mId, address, postcode, detailaddress, extraaddress, newname, newmobile);
			}
			
			return "redirect:/signUp/dvList";
		}
		
		@RequestMapping("/signUp/deliveryUp")
		public String doDeliveryUp(HttpServletRequest request, Model model) {
			HttpSession session=request.getSession();

			model.addAttribute("userType",session.getAttribute("userType"));
			model.addAttribute("mname",session.getAttribute("mName"));

			mDTO mdto=ime.userList((String)session.getAttribute("userid"));
			model.addAttribute("mdto",mdto);
			
			ArrayList<mDTO> list=ime.deliveryList((String)session.getAttribute("userid"));
			model.addAttribute("list",list);
			
			return "member/deliveryUp";
		}

		@RequestMapping("/signUp/delInformationu")
		@ResponseBody
		public String delInformationUser(HttpServletRequest request){
			HttpSession session=request.getSession();
			System.out.println("userType="+session.getAttribute("userType"));

			if(session.getAttribute("userType").equals("손님")) {
				System.out.println("회원탈퇴 usertype=" + session.getAttribute("userType"));
				ime.delInformation((String) session.getAttribute("userid"));
				ime.delDelivery((String) session.getAttribute("userid"));
			}
			session.invalidate();
			return Integer.toString(1);
		}
		@RequestMapping("/signUp/delInformation")
		@ResponseBody
		public String delInformation(HttpServletRequest request, @RequestParam("SSe") int sSe) {
			HttpSession session=request.getSession();
//			int sSe=Integer.parseInt(request.getParameter("delseq"));
			log.info("가게번호는={}",sSe);
			System.out.println("userType="+session.getAttribute("userType"));

//			if(session.getAttribute("userType").equals("손님")){
//				System.out.println("회원탈퇴 usertype="+session.getAttribute("userType"));
//				ime.delInformation((String)session.getAttribute("userid"));
//				ime.delDelivery((String)session.getAttribute("userid"));
//
//			}else if(session.getAttribute("userType").equals("사장님")){
			if(session.getAttribute("userType").equals("사장님")){

				String simg=ims.delstorelogo((String)session.getAttribute("userid"));
				System.out.println("가게로고이미지="+simg);

//				String uploadfolder = request.getServletContext().getRealPath("/static/upload/"+sSe);
				System.out.println("메뉴이미지삭제 경로는 "+uploadfolder+sSe);
				Path directoryPath = Paths.get(uploadfolder+sSe);

				//메뉴이미지 및 목록 삭제
				ArrayList<StoreDTO> sDTO=ims.delmenuimg((String)session.getAttribute("userid"));
				System.out.println("메뉴 목록 리스트="+sDTO);
				for(int i=0;i<sDTO.size();i++){
					StoreDTO list = sDTO.get(i);
					//System.out.println("i ["+i+"] ["+list.getMenuImg()+"]");
					//System.out.println("mimg="+list.getMenuImg());
					if(list !=null ){
						if(list.getMenuImg() != null) {
							System.out.println("its not null");
							File dfile = new File(String.valueOf(directoryPath), list.getMenuImg());
							dfile.delete();
							System.out.println("메뉴번호=" + i);
						}
					}
				}
				ims.deleteAllMenu((String)session.getAttribute("userid"));

				File folder = new File(String.valueOf(directoryPath));
				try {
					File[] listFiles = folder.listFiles();
					if (listFiles.length == 0 && folder.isDirectory()) {
						folder.delete();
					}
				} catch (Exception e){
					e.printStackTrace();
				}
				//찜목록 삭제
				store.zzimListDelete(sSe);


				//가게로고 및 정보 삭제
//				String deletefile = request.getServletContext().getRealPath("/static/upload");
				System.out.println("가게로고삭제 경로는 "+uploadfolder);
				if(simg!=null){
					File dfile = new File(uploadfolder,simg);
					dfile.delete();
				}
				ims.deleteStore((String)session.getAttribute("userid"));

				System.out.println("회원탈퇴 usertype="+session.getAttribute("userType"));
				ime.delInformation((String)session.getAttribute("userid"));
				ime.delDelivery((String)session.getAttribute("userid"));
			}

			session.invalidate();
			return Integer.toString(1);
		}
		
		@RequestMapping(value="/signUp/informationUpdate", method=RequestMethod.POST)
		public String information(@RequestParam("username") String name,
				@RequestParam("usermobile") String mobile,
				@RequestParam("useremail") String email,
				HttpServletRequest request) {
			HttpSession session=request.getSession();
			
			ime.updateLogin(name,mobile,email,(String)session.getAttribute("userid"));
			
			return "redirect:/signUp";
		}
		
		@RequestMapping("/signUp/informationUp")
		public String doInformationUp(HttpServletRequest request, Model model) {
			HttpSession session=request.getSession();

			model.addAttribute("userType",session.getAttribute("userType"));
			model.addAttribute("mname",session.getAttribute("mName"));

			mDTO mdto=ime.userList((String)session.getAttribute("userid"));
			model.addAttribute("mdto",mdto);
			
			return "member/informationUp";
		}
		
		@ResponseBody
		@RequestMapping(value="/signUp/pwdNew", method=RequestMethod.POST)
		public String pwdNew(@RequestParam("pwd") String pwd,
				HttpServletRequest request, Model model) {
			HttpSession session=request.getSession();

			model.addAttribute("userinfo",session.getAttribute("userid"));
			model.addAttribute("userType",session.getAttribute("userType"));

			ime.updatePwd(pwd, (String)session.getAttribute("userid"));
			
			return Integer.toString(1);
		}
		

		@ResponseBody
		@RequestMapping(value = "/signUp/pwd_check", method = RequestMethod.POST)
		public String doPwdCheck(@RequestParam("pwd") String pwd,
				HttpServletRequest request) {
			HttpSession session=request.getSession();

			int check=ime.checkId((String)session.getAttribute("userid"), pwd);

			return String.valueOf(check);
		}
		
		@RequestMapping("/signUp/checkpwd")
		public String doCheckPwd(HttpServletRequest request, Model model) {
			HttpSession session=request.getSession();

			model.addAttribute("userType",session.getAttribute("userType"));
			model.addAttribute("mname",session.getAttribute("mName"));

			mDTO mdto=ime.userList((String)session.getAttribute("userid"));
			model.addAttribute("mdto",mdto);
			
			return "member/checkpwd";
		}
		
		@RequestMapping("/signUp")
		public String dosignUp(HttpServletRequest request, Model model) {
			HttpSession session=request.getSession();

			model.addAttribute("userType",session.getAttribute("userType"));
			model.addAttribute("mname",session.getAttribute("mName"));

			mDTO mdto=ime.userList((String)session.getAttribute("userid"));
			model.addAttribute("mdto",mdto);
			
			int cnt=ime.cntAddress((String)session.getAttribute("userid"));
			model.addAttribute("cnt",cnt);
			

		    int cntStore = ims.cntStore((String)session.getAttribute("userid"));
		    model.addAttribute("cntStore",cntStore);

		    StoreDTO sVO = ims.selStore((String)session.getAttribute("userid"));
		    model.addAttribute("sVO",sVO);

			return "member/signUp";
		}
		
		@RequestMapping(value="/user_check",method = RequestMethod.POST)
		public String doUserCheck(@RequestParam("id") String id,
				@RequestParam("pwd") String pwd,
				HttpServletRequest request,
				Model model) {
			
			HttpSession session=request.getSession();
			
			int check=ime.checkId(id, pwd);


			if(check == 1) {
				session.setAttribute("userid", id);

				mDTO profile=ime.profile(id, pwd);

				session.setAttribute("mName",profile.getMName());
				session.setAttribute("mAddress",profile.getMAddress());
				session.setAttribute("mDetailaddress",profile.getMDetailAddress());
				session.setAttribute("mExtraaddress",profile.getMExtraAddress());
				session.setAttribute("mMobile",profile.getMMobile());

				if(profile.getMType() == 3) {
					session.setAttribute("userType", "손님");
				}else if(profile.getMType() == 2) {
					session.setAttribute("userType", "사장님");
				}else{
					session.setAttribute("userType","admin");
				}
			}else {
				model.addAttribute("ch","<h7>등록되지 않은 계정입니다.</h7>");
				return "member/login";
			}

			Object destination = session.getAttribute("destination");
			String redirect = destination != null ? (String) destination : (String) "/main";

			return "redirect:"+redirect;
		}

		@RequestMapping("/member/findId")
		public String findId(){
			return "member/findId";
		}
		@RequestMapping(value = "/member/findIdView", method = RequestMethod.POST)
		public String findIdView(@RequestParam("memberEmail") String email, Model model){
			log.info("email={}",email);

			if(ime.findIdCheck(email) == 0){
				model.addAttribute("msg","아이디가 존재하지않습니다, 이메일을 확인해주세요.");
				return "/member/findId";
			}else{
				model.addAttribute("member",ime.findId(email));
				return "/member/findIdView";
			}
		}
		@RequestMapping("/member/findPwd")
		public String findPwd(){
			return "member/findPwd";
		}
		@RequestMapping(value = "/member/findPwdView",method = RequestMethod.POST)
		public String findPwdView(@RequestParam("memberId") String mid,
								  @RequestParam("memberEmail") String email,
								  Model model) throws MessagingException, UnsupportedEncodingException {

			if(ime.findPwdCheck(email,mid) == 0){
				model.addAttribute("msg","아이디와 이메일을 확인해주세요.");
				return "/member/findPwd";
			}else{
				String memberKey = new TempKey().getKey(6,false);
				MailUtils sendMail = new MailUtils(javaMailSender);
//				MimeMessage mimeMessage = javaMailSender.createMimeMessage();
//				MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage,true,"UTF-8");
				sendMail.setFrom(fromMail,"빠르조");
				sendMail.setTo(email);
				sendMail.setSubject("배달의 민족 임시 비밀번호 입니다.");
				sendMail.setText(
						"<h1>임시비밀번호 발급</h1>" +
								"<br/>"+mid+"님 "+
								"<br/>배달의 민족 임시 비밀번호입니다."+
								"<br/>임시비밀번호 :   <h2>"+memberKey+"</h2>"+
								"<br/>로그인 후 비밀번호 변경을 해주세요.");
				sendMail.send();
				log.info("임시비밀번호={}",memberKey);
				ime.findPwd(memberKey,email,mid);

				model.addAttribute("member",email);
				return "/member/findPwdView";
			}
//			return "member/findPwdView";
		}

		@RequestMapping("/errorLogin")
		public String errorLogin(HttpServletRequest request, Model model) {
			return "member/errorLogin";
		}
		@RequestMapping("/login")
		public String doLogin(HttpServletRequest request, Model model) {
			return "member/login";
		}
		
		@RequestMapping("/logout")
		public String doLogout(HttpServletRequest req) {
			HttpSession session=req.getSession();
			session.invalidate();
			return "redirect:/main";
		}
		
		@RequestMapping(value = "/usersign", method = RequestMethod.POST)
		public String usersign(@RequestParam("userid") String id,
				@RequestParam("userpwd") String pwd,
				@RequestParam("username") String name,
				@RequestParam("postcode") String postcode,
				@RequestParam("address") String address,
				@RequestParam("detailAddress") String detailaddress,
				@RequestParam("extraAddress") String extraaddress,
				@RequestParam("usermobile") String mobile,
				@RequestParam("useremail") String mail,
				@RequestParam("mType") int type) {
			
			ime.addMember(id,pwd,name,mobile,type,postcode,address,detailaddress,extraaddress,mail);
			ime.addDelivery(id, address, postcode, detailaddress, extraaddress,name,mobile);
			
			return "member/login";
		}
		
		@ResponseBody
		@RequestMapping(value = "/idoverlap")
		public String idOverlap(@RequestParam("userid") String id) {

			int check=ime.idOverlap(id);
			return Integer.toString(check);
		}
		
		@RequestMapping("/userSign")
		public String userSign(@RequestParam("Sign") String type,
							   HttpServletRequest request, Model model) {
			HttpSession session=request.getSession();

			model.addAttribute("userType",session.getAttribute("userType"));
			model.addAttribute("mname",session.getAttribute("mName"));

			model.addAttribute("type",type);
			return "member/userSign";
		}
		
		@RequestMapping("/signin")
		public String doSignin(HttpServletRequest request, Model model) {
			HttpSession session=request.getSession();

			model.addAttribute("userType",session.getAttribute("userType"));
			model.addAttribute("mname",session.getAttribute("mName"));

			return "member/signin";
		}


		@RequestMapping("/memberlist")
		public String doMemberlist(){



			return "member/memberlist";
		}
}

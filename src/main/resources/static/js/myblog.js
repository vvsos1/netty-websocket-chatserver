$(document).ready(function() {
	$('#regForm').submit(function() {
		// 자동 submit되는 기능을 막음
		event.preventDefault();

		var id = $('#id1').val();
		var pwd = $('#password1').val();
		var classroom = $("select[name=class1]").val();
		var grade = $(":input:radio[name=grade1]:checked").val();
		var number = $('#studentId1').val();
		var name = $('#studentName1').val();

		console.log('id : ' + id);
		console.log('password : ' + pwd);
		console.log('classroom : ' + classroom);
		console.log('grade : ' + grade);
		console.log('studentId : ' + number);
		console.log('studentName : ' + name);

		// 입력칸 중 빈칸이 있는 경우
		// if (id == "" || pwd == "" || classroom == "" || grade == "" || number
		// == "" || name == "") {
		// return;
		// }

		// 서버로 회원가입 정보 보내기
		$.post(contextPath + "/signup.do", {
			id : id,
			pwd : pwd,
			classroom : classroom,
			grade : grade,
			number : number,
			name : name
		}, function(data) {
			if (data.result == "success") { // 회원가입 성공한 경우
				// 회원가입에 성공했으므로 회원가입 Modal 숨기기
				$('#regModal').modal('hide');
				// 회원가입 성공 Modal 띄우기
				alertModal("회원가입 정보", data.userName + "님 회원가입되었습니다.");
				// 회원가입 성공했으므로 폼에 있는 데이터 삭제
				$('#password1').val('');
			} else if (data.result == "fail") { // 회원가입에 실패한 경우
				alertModal("회원가입 정보", data.msg);
				document.regForm.password.reset();
			} else { // 그 이외 경우(EX)서버가 다운된 경우)
				alertModal("회원가입 정보", "알 수 없는 오류가 발생했습니다.");
			}
		});
	});

	$('#loginForm').submit(function() {
		// 자동 submit되는 기능을 막음

		event.preventDefault();

		var loginId = document.getElementById('loginId').value;
		var loginPassword = document.getElementById('loginPassword').value;

		// 만약 아이디 또는 비밀번호를 입력하지 않았다면
		if (loginId == "" || loginPassword == "") {
			return;
		}

		// 서버로 로그인 정보 보내기
		$.post(contextPath + "/login.do", {
			id : loginId,
			pwd : loginPassword
		}, function(data) {
			console.log("로그인 요청 응답 : " + data);
			if (data.result == "success") {
				location.reload();
			} else if (data.result == "fail") {
				alertModal("로그인 정보", data.msg);
			} else {
				alertModal("로그인 정보", "알 수 없는 오류가 발생했습니다.")
			}
		});

	});
	
	
	$('#myInfoForm').submit(function() {
		// 자동 submit되는 기능을 막음
		event.preventDefault();

		var id = $('#id2').val();
		var oldPwd = $('#oldPassword').val();
		var newPwd = $('#newPassword').val();
		var classroom = $("select[name=class2]").val();
		var grade = $(":input:radio[name=grade]:checked").val();
		var number = $('#studentId2').val();
		var name = $('#studentName2').val();

		console.log('id : ' + id);
		console.log('oldPassword : ' + password);
		console.log('newPassword : ' + password);
		console.log('classroom : ' + classroom);
		console.log('grade : ' + grade);
		console.log('studentId : ' + studentId);
		console.log('studentName : ' + studentName);

		// 입력칸 중 빈칸이 있는 경우
		// if (id == "" || pwd == "" || classroom == "" || grade == "" || number
		// == "" || name == "") {
		// return;
		// }

		// 서버로 회원가입 정보 보내기
		$.post(contextPath + "/modify.do", {
			id : id,
			oldPwd : oldPwd,
			newPwd : newPwd,
			classroom : classroom,
			grade : grade,
			number : number,
			name : name
		}, function(data) {
			if (data.result == "success") { // 회원정보 수정에 성공한 경우

				$('#myInfoModal').modal('hide');
				// 회원정보 수정 성공 Modal 띄우기
				alertModal("정보","회원정보 수정에 성공했습니다.");
				// 회원정보 수정에 성공했으므로 폼에 있는 데이터 삭제
				document.regForm.reset();
				location.reload();
			} else if (data.result == "fail") { // 회원정보 수정에 실패한 경우
				alertModal("오류", data.msg);
				$('#oldPassword').val('');
				$('#newPassword').val('');
			} else { // 그 이외 경우(EX)서버가 다운된 경우)
				alertModal("오류", "알 수 없는 오류가 발생했습니다.");
			}
		});
	});
});


function logout() {

	// 서버로 로그아웃 요청 보내기
	$.post(contextPath + "/logout.do", {}, function(data) {
		console.log("로그아웃 요청");
		location.reload();
	});

}

// function login() {
// var loginId = document.getElementById('loginId').value;
// var loginPassword = document.getElementById('loginPassword').value;

// // 만약 아이디 또는 비밀번호를 입력하지 않았다면
// if (loginId == "" || loginPassword == "") {
// return;
// }

// var localUser = JSON.parse(localStorage.getItem(loginId));
// // 만약 아이디 또는 비밀번호가 다르다면
// if (localUser.id != loginId || localUser.password != loginPassword) {
// alertModal("로그인 오류", "아이디 또는 비밀번호가 잘못되었습니다.");
// return;
// } else { // 모두 맞다면
// sessionStorage.setItem('loginUser', localStorage.getItem(loginId));
// alertModal('로그인 정보', loginId+"님 로그인되었습니다.");
// checkSession();
// }

// }

// Modal로 Alert를 띄워줌
function alertModal(title, body) {
	// Modal 가져오기
	var myModal = $("#alertModal");
	// Modla 띄우기
	myModal.modal();
	// Modal 안의 내용 설정
	myModal.find('.modal-title').text(title);
	myModal.find('.modal-body').text(body)
}
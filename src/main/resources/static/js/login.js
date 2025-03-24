

function loginWithKakao() {
    Kakao.Auth.authorize({
       prompt: 'select_account',
       redirectUri: "http://secretm-yummy.com:8081/login",
    });
  }

  window.onload = function(){
    //   Kakao.init('4a2a51c4104deceb54f805eb34bc4f3d'); // 사용하려는 앱의 JavaScript 키 입력
    updateBodyPadding();
      const param_code = GetLoginCode();
      if(!!param_code){
          KaKaoLogin(param_code);
      }
      else{
          GetKakoUserInfoByAccessToken();
      }
  }

  function updateBodyPadding() {
        const body = document.body;
        const width = window.innerWidth;

        if (width <= 480) {
            // 작은 모바일
            body.style.paddingTop = '80px';
            body.style.paddingBottom = '190px';
        }
    }

  function GetLoginCode(){
      const parm = new URLSearchParams(window.location.search);
      const code = parm.get('code');
      return code;
  }

  async function KaKaoLogin(code){
      const response = await fetch('/login/kakao/callback',
                                  {
                                      method:'POST',
                                      headers:{'Content-Type':'application/json'},
                                      body:JSON.stringify({code:code}),
                                      credentials: 'include' // 쿠키 설정을 하기 위함
                                  });

      const tokens = await response.json();
      if(!!tokens){
          const access_token = tokens.kakao_access_token;
          const userinfo = tokens.kakao_payload;

          if(!!access_token){
           Kakao.Auth.setAccessToken(access_token);
           document.getElementById("login-container").style.display = 'none';
           document.getElementById("profile-image").src = userinfo.picture;
           document.getElementById("nickname").innerText = userinfo.nickname;
           document.getElementById("success-container").style.display = 'block';

          }
      }else{
          alert('로그인 실패 다시 시도해주세요');
      }
  }

  //error 확인
  //서비스 함수 호출해서 쿠키 날려줘야 한다.
  async function KaKaoLogout(){
      Kakao.Auth.logout().then(function(response){
          if(!Kakao.Auth.getAccessToken()){
              alert('로그아웃 되었습니다.');
              document.getElementById("success-container").style.display = 'none';
              document.getElementById("nickname").innerText ="";
              document.getElementById("profile-image").src = "";
              document.getElementById("login-container").style.display = 'block';

              const url ='/auth/logout';
              const response =  fetch(url,
                                            {
                                                method:'POST',
                                                headers:{'Content-Type':'application/json'},
                                                credentials: 'include'
                                            });

            setTimeout(() => {
                window.location.href = "/login";
            }, 500);
          }
      })
      .catch(function(err){
          console.log('exception'+err);
          alert('로그아웃 실패하였습니다.');
      });
   }

  function IsKakaoToken(){
      const access_token = Kakao.Auth.getAccessToken();
      //const l_accessToken = localStorage.getItem('accessToken');

      // if(!!access_token || !!l_accessToken) {
      //     if(access_token === null && !!l_accessToken){
      //         Kakao.Auth.setAccessToken(l_accessToken);
      //     }else if(!!access_token && l_accessToken === null){
      //         localStorage.setItem('accessToken', access_token);
      //     }

      //     return true;
      // }
      if(!!access_token) return true;
      else return false;
  }

  async function GetKakoUserInfoByAccessToken()
  {
//    const session_res = await fetch('/auth/session'); // 📌 현재 로그인한 사용자 정보 가져오기
//    const user = await session_res.json();

//    if (user && user.error_code === '999'){
        let access_token = Kakao.Auth.getAccessToken();
        //if(!!access_token){
          const url ='/login/kakao/GetUserInfoByToken';
          const response = await fetch(url,
                                      {
                                          method:'POST',
                                          headers:{'Content-Type':'application/json'},
                                          body:access_token,
                                          credentials: 'include'
                                      });

          const token_data = await response.json();
          console.log(token_data);

          const new_access_token = token_data.access_token;
          const nickname = token_data.nickname;
          const picture = token_data.picture;

          if(new_access_token){
              Kakao.Auth.setAccessToken(new_access_token);
          }

//            setTimeout(() => {
//                 window.location.href = "/login";
//          }, 500);
//    }
  }

  async function RegisterAddress(){

    const address_type = document.querySelector("input[name='address-type']:checked");
    const address_type_value = address_type ? address_type.value : null
    const address = document.getElementById("address").value;
    const user_name = window.env.login_user.name;

    if(!user_name){
        alert('로그인 이후 사용해주세요');
        return;
    }

    if(!address_type_value){
        alert('주소타입을 선택해주세요');
        return;
    }

    if(!address){
        alert('주소를 입력해 주세요');
        return;
    }

    const address_json = {
                            address_type:address_type_value,
                            address:address
                         }
    const url ='/auth/AddUserDetail';
    const response = await fetch(url,
                                {
                                    method:'POST',
                                    headers:{'Content-Type':'application/json'},
                                    body:JSON.stringify(address_json),
                                    credentials: 'include'
                                });

    const detail = await response.json();
    if(detail){
        alert('성공');
        window.location.reload();
    }
    //console.log(detail);
}

function toggleAddressAdd() {
    var area = document.getElementById("registration-area");
    if (area.style.display === "none" || area.style.display === "") {
      area.style.display = "block";
    } else {
      area.style.display = "none";
    }
  }

function SearchLayerToggle() {
    // iframe을 넣은 element를 안보이게 한다.
    document.getElementById('layer').style.display = 'none';
}

function SearchAddressLayer() {
    new daum.Postcode({
        oncomplete: function(data) {
            // 검색결과 항목을 클릭했을때 실행할 코드를 작성하는 부분.
            // 각 주소의 노출 규칙에 따라 주소를 조합한다.
            // 내려오는 변수가 값이 없는 경우엔 공백('')값을 가지므로, 이를 참고하여 분기 한다.
            var addr = ''; // 주소 변수
            var extraAddr = ''; // 참고항목 변수

            //사용자가 선택한 주소 타입에 따라 해당 주소 값을 가져온다.
            if (data.userSelectedType === 'R') { // 사용자가 도로명 주소를 선택했을 경우
                addr = data.roadAddress;
            } else { // 사용자가 지번 주소를 선택했을 경우(J)
                addr = data.jibunAddress;
            }

            // 사용자가 선택한 주소가 도로명 타입일때 참고항목을 조합한다.
            if(data.userSelectedType === 'R'){
                // 법정동명이 있을 경우 추가한다. (법정리는 제외)
                // 법정동의 경우 마지막 문자가 "동/로/가"로 끝난다.
                if(data.bname !== '' && /[동|로|가]$/g.test(data.bname)){
                    extraAddr += data.bname;
                }
                // 건물명이 있고, 공동주택일 경우 추가한다.
                if(data.buildingName !== '' && data.apartment === 'Y'){
                    extraAddr += (extraAddr !== '' ? ', ' + data.buildingName : data.buildingName);
                }
                // 표시할 참고항목이 있을 경우, 괄호까지 추가한 최종 문자열을 만든다.
                if(extraAddr !== ''){
                    extraAddr = ' (' + extraAddr + ')';
                }
                // 조합된 참고항목을 해당 필드에 넣는다.
                //document.getElementById("sample2_extraAddress").value = extraAddr;

            } else {
                //document.getElementById("sample2_extraAddress").value = '';
            }

            // 우편번호와 주소 정보를 해당 필드에 넣는다.
            // document.getElementById('sample2_postcode').value = data.zonecode;
             document.getElementById("address").value = addr;
            // 커서를 상세주소 필드로 이동한다.
            //document.getElementById("sample2_detailAddress").focus();

            // iframe을 넣은 element를 안보이게 한다.
            // (autoClose:false 기능을 이용한다면, 아래 코드를 제거해야 화면에서 사라지지 않는다.)
            document.getElementById('layer').style.display = 'none';
        },
        width : '100%',
        height : '100%',
        maxSuggestItems : 5
    }).embed(document.getElementById('layer'));

    // iframe을 넣은 element를 보이게 한다.
    document.getElementById('layer').style.display = 'block';

    // iframe을 넣은 element의 위치를 화면의 가운데로 이동시킨다.
    initLayerPosition();
}

// 브라우저의 크기 변경에 따라 레이어를 가운데로 이동시키고자 하실때에는
// resize이벤트나, orientationchange이벤트를 이용하여 값이 변경될때마다 아래 함수를 실행 시켜 주시거나,
// 직접 element_layer의 top,left값을 수정해 주시면 됩니다.
function initLayerPosition(){
    var width = 300; //우편번호서비스가 들어갈 element의 width
    var height = 400; //우편번호서비스가 들어갈 element의 height
    var borderWidth = 5; //샘플에서 사용하는 border의 두께

    // 위에서 선언한 값들을 실제 element에 넣는다.
    document.getElementById('layer').style.width = width + 'px';
    document.getElementById('layer').style.height = height + 'px';
    document.getElementById('layer').style.border = borderWidth + 'px solid';
    // 실행되는 순간의 화면 너비와 높이 값을 가져와서 중앙에 뜰 수 있도록 위치를 계산한다.
    document.getElementById('layer').style.left = (((window.innerWidth || document.documentElement.clientWidth) - width)/2 - borderWidth) + 'px';
    document.getElementById('layer').style.top = (((window.innerHeight || document.documentElement.clientHeight) - height)/2 - borderWidth) + 'px';
}

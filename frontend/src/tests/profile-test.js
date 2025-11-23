// 这是一个测试文件，包含正确语法的saveProfile方法实现
function saveProfileTest() {
  console.error('========== 开始保存用户资料 ==========');
  
  // 模拟用户数据
  var userData = { id: 1 };
  var formData = { username: 'admin', phone: '18178108914', address: '新梅大道99号' };
  
  console.error('用户ID:', userData.id);
  
  var updateData = {
    username: formData.username,
    phone: formData.phone,
    address: formData.address
  };
  
  console.error('更新数据:', JSON.stringify(updateData));
  
  // 获取token
  var token = localStorage.getItem('userToken');
  console.error('Token存在:', !!token);
  
  var apiUrl = 'http://localhost:8080/api/user/' + userData.id;
  console.error('请求URL:', apiUrl);
  
  // 使用XMLHttpRequest
  var xhr = new XMLHttpRequest();
  xhr.open('PUT', apiUrl, true);
  xhr.setRequestHeader('Content-Type', 'application/json');
  
  if (token) {
    xhr.setRequestHeader('Authorization', 'Bearer ' + token);
    console.error('已设置Authorization头');
  }
  
  xhr.onreadystatechange = function() {
    if (xhr.readyState === 4) {
      console.error('XHR状态码:', xhr.status);
      console.error('XHR响应文本:', xhr.responseText);
      
      if (xhr.status >= 200 && xhr.status < 300) {
        try {
          var response = JSON.parse(xhr.responseText);
          console.error('响应解析成功:', JSON.stringify(response));
          console.error('========== 保存成功 ==========');
          alert('资料更新成功');
        } catch (e) {
          console.error('响应解析失败:', e.message);
          console.error('========== 保存失败 ==========');
          alert('资料更新失败: 响应格式错误');
        }
      } else {
        var errorInfo = xhr.responseText || '无响应内容';
        console.error('错误响应:', errorInfo);
        console.error('========== 保存失败 ==========');
        alert('资料更新失败: HTTP错误 ' + xhr.status);
      }
    }
  };
  
  xhr.onerror = function() {
    console.error('网络请求错误');
    console.error('========== 保存失败 ==========');
    alert('资料更新失败: 网络错误');
  };
  
  xhr.send(JSON.stringify(updateData));
  console.error('请求已发送');
  console.error('========== 保存操作完成 ==========');
}
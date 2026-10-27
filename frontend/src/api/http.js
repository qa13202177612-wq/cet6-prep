import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

const http = axios.create({ baseURL: '/api', timeout: 30000 })

// 统一拆包:后端所有接口都是 {code, msg, data};非 0 一律弹错
http.interceptors.response.use(
  (res) => {
    const body = res.data
    if (body && body.code === 0) return body.data
    if (body && body.msg === '未登录') {
      localStorage.removeItem('user')
      router.push('/login')
      return Promise.reject(new Error('未登录'))
    }
    ElMessage.error(body?.msg || '请求失败')
    return Promise.reject(new Error(body?.msg))
  },
  (err) => {
    ElMessage.error('网络错误:' + err.message)
    return Promise.reject(err)
  }
)

export default http

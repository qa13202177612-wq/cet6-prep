import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '../views/LoginView.vue'
import HomeView from '../views/HomeView.vue'
import WordStudy from '../views/WordStudy.vue'
import SkillsView from '../views/SkillsView.vue'
import SentenceView from '../views/SentenceView.vue'
import ListeningView from '../views/ListeningView.vue'
import ReadingView from '../views/ReadingView.vue'
import WritingView from '../views/WritingView.vue'
import ExamView from '../views/ExamView.vue'
import ReportView from '../views/ReportView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', component: LoginView },
    { path: '/', component: HomeView },
    { path: '/words', component: WordStudy },
    { path: '/skills', component: SkillsView },
    { path: '/sentence', component: SentenceView },
    { path: '/listening', component: ListeningView },
    { path: '/reading', component: ReadingView },
    { path: '/writing', component: WritingView },
    { path: '/exam', component: ExamView },
    { path: '/report', component: ReportView }
  ]
})

// 未登录全部赶去登录页
router.beforeEach((to) => {
  const logged = !!localStorage.getItem('user')
  if (!logged && to.path !== '/login') return '/login'
  if (logged && to.path === '/login') return '/'
})

export default router

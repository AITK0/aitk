import Vue from 'vue'
import Router from 'vue-router'

Vue.use(Router)

export default new Router({
    mode:'hash',
    base: process.env.BASE_URL,
    routes: [
        {
            path: '/',
            name: 'home',
            component:  () => import("../views/Home.vue")
        },
        {
            path: '/common',
            name: 'Common',
            component: () => import("../views/Common.vue")
        },
        {
            path: '/llm',
            name: 'Llm',
            component: () => import("../views/Llm.vue")
        }
    ]
})
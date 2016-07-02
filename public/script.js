document.addEventListener('DOMContentLoaded', init);
function init() {
  Vue.use(VueMdl.default)
  var TaskList = Vue.extend({
    props: ["project","filter"],
    template: `
      <div v-if="project">
        <h1>{{project.name}}のタスク一覧</h1>
        <div class="task-container">
          <mdl-card class="task mdl-card mdl-shadow--2dp status-{{item.status}}"
            :style="{display:item.description.match(filter)?'':'none'}"
            data-status="{{item.status}}"
            v-for="item in tasks" supporting-text="supportingText">
            <div class="mdl-card__title" slot="title">
              <h2 class="mdl-card__title-text" contentEditable
                @keypress.prevent.enter="modifyTask($event, item)">
                {{item.description}}</h2>
            </div>
          </mdl-card>
        </div>
      </div>

    `,
    data: function () {
      return {
        tasks: []
      };
    },
    init: function () {},
    ready: function () {
      if (this.project != null)
        this.call();
      this.$watch("project", this.call);
    },
    methods: {
      call: function () {
        superagent.get("/task/" + this.project.id).set('Accept', 'application/json').end(
          (err, res) => {
            this.tasks = JSON.parse(res.text).reverse();
          }
        );
      },
      //TODO 削除：使ってない
      addTask:function(){
        if(!this.description)return;
        superagent.post("/task").set('Accept', 'application/json')
          .send({project:this.project.id, description:this.description})
          .end((err, res) => this.call());
        this.description=""
      },
      modifyTask:function($event, task){
        task.description=$event.target.innerText;
        superagent.put("/task")
        .set('Accept', 'application/json')
        .send(task)
        .end((err, res) => this.call());
      }
    }
  })
  Vue.component('task-list', TaskList);
  var ProjectList = Vue.extend({
  props: ["selectedProject"],
  template: `
      <nav class="mdl-navigation" v-for="project in projects" v-on:click="select(project)" >
        <a class="mdl-navigation__link"  contentEditable="true" @keypress.prevent.enter="changeName($event,project)">{{ project.name }}</a>
      </nav>
      <input type="text" v-model="newName" @keypress.enter="newProject"></input>
      <mdl-button v-mdl-ripple-effect @click="newProject">New Project</mdl-button>
    `,
    data: function () {
      return {projects: [],newName:""};
    },
    init: function () {
      superagent.get("/project")
      .set('Accept', 'application/json')
      .end((err, res) => {
        this.projects = JSON.parse(res.text);
        this.selectedProject = this.projects[0]
      });
    },
    methods: {
      select: function (project) {
        this.selectedProject = project
      },

      newProject: function(){
        if(!this.newName)return;
        superagent.post("/project")
        .set('Accept','application/json')
        .send({name:this.newName})
        .end((err,res)=>{
          if(err)return;
          var project=JSON.parse(res.text);
          this.projects.push(project);
          this.selectedProject=project;
        });
        this.newName="";
      },
      changeName:function(e,project){
        project.name=e.target.innerText
        superagent.put("/project")
        .set('Accept','application/json')
        .send(project)
        .end((err,res)=>{})
      }
    }
  })
  Vue.component('project-list', ProjectList);
  var MyHeader=Vue.extend({
    props: ["project","searchText"],
    template:`
      <header class="mdl-layout__header">
        <div class="mdl-layout__header-row"><span class="header-title">Project Manager</span>
          <div class="mdl-layout-spacer"></div>
          <mdl-textfield :value.sync="newTaskName" @keypress.prevent.enter="hoge" @focusout="clearText('newTaskName')" id="add" expandable="add" class="mdl-textfield--align-right"></mdl-textfield>
          <mdl-textfield id="search" :value.sync="searchText" expandable="search" class="mdl-textfield--align-right" debounce="500"></mdl-textfield>
        </div>
      </header>`,
    data:function(){
      return {newTaskName:""};
    },
    methods:{
      hoge:function(){
        if(!this.newTaskName)return;
        superagent.post("/task")
          .set('Accept', 'application/json')
          .send({project:this.project.id, description:this.newTaskName})
          .end((err, res) => this.$emit("addTask"));
        this.newTaskName=""
      },
      clearText:function(key){this[key]="";}
    }
  })
  Vue.component('my-header',MyHeader)
  var App = new (Vue.extend({
    data:function(){
      return {
        selectedProject:null,
        searchText:""
      }
    },
    ready:function(){
      this.$refs.header.$on("addTask",this.$refs.tasks.call);
    }
  }))({
    el: "#app",
    components:VueMdl.components,
    directives:VueMdl.directives,
  });
  window.e=App;
  console.log("test");
}
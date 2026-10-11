import React, { useState, useEffect } from 'react';
import Navbar from './components/Navbar';
import AuthModal from './components/AuthModal';
import TaskBoard from './components/TaskBoard';
import TaskModal from './components/TaskModal';
import CreateTaskModal from './components/CreateTaskModal';
import CreateOrgModal from './components/CreateOrgModal';
import CreateProjectModal from './components/CreateProjectModal';
import OrgMembersModal from './components/OrgMembersModal';
import { api, getStoredUser, removeAuthToken } from './api/client';
import { Building2, Briefcase, Plus, Sparkles, CheckCircle2 } from 'lucide-react';

export default function App() {
  const [user, setUser] = useState(getStoredUser());
  const [organizations, setOrganizations] = useState([]);
  const [activeOrg, setActiveOrg] = useState(null);
  const [orgMembers, setOrgMembers] = useState([]);
  
  const [projects, setProjects] = useState([]);
  const [activeProject, setActiveProject] = useState(null);

  const [tasks, setTasks] = useState([]);
  const [loadingTasks, setLoadingTasks] = useState(false);

  const [notifications, setNotifications] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);

  // Modals state
  const [selectedTask, setSelectedTask] = useState(null);
  const [isCreateTaskOpen, setIsCreateTaskOpen] = useState(false);
  const [createTaskInitialStatus, setCreateTaskInitialStatus] = useState('TODO');
  const [isCreateOrgOpen, setIsCreateOrgOpen] = useState(false);
  const [isCreateProjectOpen, setIsCreateProjectOpen] = useState(false);
  const [isOrgMembersOpen, setIsOrgMembersOpen] = useState(false);

  // Listen to auth expired event
  useEffect(() => {
    const handleAuthExpired = () => {
      setUser(null);
    };
    window.addEventListener('devflow_auth_expired', handleAuthExpired);
    return () => window.removeEventListener('devflow_auth_expired', handleAuthExpired);
  }, []);

  // When user logs in or is present, fetch initial data
  useEffect(() => {
    if (user) {
      loadOrganizations();
      loadNotifications();
    }
  }, [user]);

  // When active organization changes, fetch its projects and members
  useEffect(() => {
    if (activeOrg) {
      loadProjects(activeOrg.id);
      loadOrgMembers(activeOrg.id);
    } else {
      setProjects([]);
      setActiveProject(null);
      setOrgMembers([]);
    }
  }, [activeOrg]);

  // When active project changes, fetch its tasks
  useEffect(() => {
    if (activeOrg && activeProject) {
      loadTasks(activeOrg.id, activeProject.id);
    } else {
      setTasks([]);
    }
  }, [activeOrg, activeProject]);

  const loadOrganizations = async () => {
    try {
      const data = await api.organizations.list();
      setOrganizations(data || []);
      if (data && data.length > 0) {
        // Retain current or set first
        setActiveOrg(prev => {
          const found = data.find(o => o.id === prev?.id);
          return found || data[0];
        });
      } else {
        setActiveOrg(null);
      }
    } catch (err) {
      console.error('Failed to load organizations', err);
    }
  };

  const loadOrgMembers = async (orgId) => {
    try {
      const data = await api.organizations.get(orgId);
      setOrgMembers(data.members || []);
    } catch (err) {
      console.error('Failed to load org members', err);
    }
  };

  const loadProjects = async (orgId) => {
    try {
      const data = await api.projects.list(orgId);
      setProjects(data || []);
      if (data && data.length > 0) {
        setActiveProject(prev => {
          const found = data.find(p => p.id === prev?.id);
          return found || data[0];
        });
      } else {
        setActiveProject(null);
      }
    } catch (err) {
      console.error('Failed to load projects', err);
    }
  };

  const loadTasks = async (orgId, projectId) => {
    setLoadingTasks(true);
    try {
      const data = await api.tasks.list(orgId, projectId);
      setTasks(data || []);
    } catch (err) {
      console.error('Failed to load tasks', err);
    } finally {
      setLoadingTasks(false);
    }
  };

  const loadNotifications = async () => {
    try {
      const data = await api.notifications.list(false, 0, 15);
      const items = data.content || [];
      setNotifications(items);
      setUnreadCount(items.filter(n => !n.read).length);
    } catch (err) {
      console.error('Failed to load notifications', err);
    }
  };

  const handleAuthSuccess = (authData) => {
    setUser({
      id: authData.userId,
      email: authData.email,
      role: authData.role
    });
  };

  const handleLogout = () => {
    removeAuthToken();
    setUser(null);
    setOrganizations([]);
    setActiveOrg(null);
    setProjects([]);
    setActiveProject(null);
    setTasks([]);
  };

  const handleTaskCreated = (newTask) => {
    setTasks([newTask, ...tasks]);
  };

  const handleTaskUpdated = (updatedTask) => {
    setTasks(tasks.map(t => t.id === updatedTask.id ? updatedTask : t));
    setSelectedTask(updatedTask);
  };

  const handleTaskDeleted = (taskId) => {
    setTasks(tasks.filter(t => t.id !== taskId));
    setSelectedTask(null);
  };

  const handleOrgCreated = (newOrg) => {
    setOrganizations([...organizations, newOrg]);
    setActiveOrg(newOrg);
  };

  const handleProjectCreated = (newProject) => {
    setProjects([...projects, newProject]);
    setActiveProject(newProject);
  };

  const handleMarkNotificationRead = async (id) => {
    try {
      await api.notifications.markRead(id);
      setNotifications(notifications.map(n => n.id === id ? { ...n, read: true } : n));
      setUnreadCount(prev => Math.max(0, prev - 1));
    } catch (err) {
      console.error('Failed to mark read', err);
    }
  };

  const handleMarkAllNotificationsRead = async () => {
    try {
      await api.notifications.markAllRead();
      setNotifications(notifications.map(n => ({ ...n, read: true })));
      setUnreadCount(0);
    } catch (err) {
      console.error('Failed to mark all read', err);
    }
  };

  if (!user) {
    return <AuthModal onAuthSuccess={handleAuthSuccess} />;
  }

  return (
    <div style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
      {/* Top Navigation */}
      <Navbar
        user={user}
        onLogout={handleLogout}
        organizations={organizations}
        activeOrg={activeOrg}
        onSelectOrg={setActiveOrg}
        onOpenCreateOrg={() => setIsCreateOrgOpen(true)}
        projects={projects}
        activeProject={activeProject}
        onSelectProject={setActiveProject}
        onOpenCreateProject={() => setIsCreateProjectOpen(true)}
        onOpenOrgMembers={() => setIsOrgMembersOpen(true)}
        notifications={notifications}
        unreadCount={unreadCount}
        onMarkNotificationRead={handleMarkNotificationRead}
        onMarkAllNotificationsRead={handleMarkAllNotificationsRead}
      />

      {/* Main Workspace View */}
      <main style={{ flex: 1 }}>
        {organizations.length === 0 ? (
          // No Organization Empty State
          <div style={{
            maxWidth: '540px',
            margin: '4rem auto',
            padding: '2.5rem',
            textAlign: 'center'
          }} className="glass-panel animate-fade-in">
            <div style={{
              width: '56px',
              height: '56px',
              borderRadius: '16px',
              background: 'linear-gradient(135deg, #6366f1, #06b6d4)',
              display: 'inline-flex',
              alignItems: 'center',
              justifyContent: 'center',
              marginBottom: '1.25rem',
              boxShadow: '0 0 25px rgba(99, 102, 241, 0.4)'
            }}>
              <Building2 size={28} color="#fff" />
            </div>
            <h2 style={{ fontSize: '1.5rem', marginBottom: '0.5rem' }}>Create your first organization</h2>
            <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem', marginBottom: '1.75rem', lineHeight: 1.6 }}>
              DevFlow organizes work around teams and organizations. Create an organization to start collaborating, adding projects, and tracking tasks.
            </p>
            <button 
              id="empty-create-org-btn"
              onClick={() => setIsCreateOrgOpen(true)} 
              className="btn-primary" 
              style={{ padding: '0.8rem 1.5rem', fontSize: '0.95rem' }}
            >
              <Plus size={18} /> Create Organization
            </button>
          </div>
        ) : projects.length === 0 ? (
          // No Project Empty State
          <div style={{
            maxWidth: '540px',
            margin: '4rem auto',
            padding: '2.5rem',
            textAlign: 'center'
          }} className="glass-panel animate-fade-in">
            <div style={{
              width: '56px',
              height: '56px',
              borderRadius: '16px',
              background: 'linear-gradient(135deg, #8b5cf6, #f59e0b)',
              display: 'inline-flex',
              alignItems: 'center',
              justifyContent: 'center',
              marginBottom: '1.25rem',
              boxShadow: '0 0 25px rgba(139, 92, 246, 0.4)'
            }}>
              <Briefcase size={28} color="#fff" />
            </div>
            <h2 style={{ fontSize: '1.5rem', marginBottom: '0.5rem' }}>No projects in {activeOrg.name} yet</h2>
            <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem', marginBottom: '1.75rem', lineHeight: 1.6 }}>
              Set up your first project board to start assigning tasks, logging activity, and monitoring team progress.
            </p>
            <button 
              id="empty-create-project-btn"
              onClick={() => setIsCreateProjectOpen(true)} 
              className="btn-primary" 
              style={{ padding: '0.8rem 1.5rem', fontSize: '0.95rem' }}
            >
              <Plus size={18} /> Create New Project
            </button>
          </div>
        ) : (
          // Active Kanban Task Board
          <TaskBoard
            project={activeProject}
            tasks={tasks}
            orgMembers={orgMembers}
            onTaskClick={(task) => setSelectedTask(task)}
            onOpenCreateTask={(colId = 'TODO') => {
              setCreateTaskInitialStatus(colId);
              setIsCreateTaskOpen(true);
            }}
          />
        )}
      </main>

      {/* Task Detail Modal */}
      {selectedTask && (
        <TaskModal
          task={selectedTask}
          orgId={activeOrg.id}
          projectId={activeProject.id}
          orgMembers={orgMembers}
          onClose={() => setSelectedTask(null)}
          onTaskUpdated={handleTaskUpdated}
          onTaskDeleted={handleTaskDeleted}
        />
      )}

      {/* Create Task Modal */}
      {isCreateTaskOpen && activeOrg && activeProject && (
        <CreateTaskModal
          isOpen={isCreateTaskOpen}
          onClose={() => setIsCreateTaskOpen(false)}
          orgId={activeOrg.id}
          projectId={activeProject.id}
          orgMembers={orgMembers}
          initialStatus={createTaskInitialStatus}
          onTaskCreated={handleTaskCreated}
        />
      )}

      {/* Create Org Modal */}
      <CreateOrgModal
        isOpen={isCreateOrgOpen}
        onClose={() => setIsCreateOrgOpen(false)}
        onOrgCreated={handleOrgCreated}
      />

      {/* Create Project Modal */}
      {activeOrg && (
        <CreateProjectModal
          isOpen={isCreateProjectOpen}
          onClose={() => setIsCreateProjectOpen(false)}
          orgId={activeOrg.id}
          onProjectCreated={handleProjectCreated}
        />
      )}

      {/* Org Members Modal */}
      {activeOrg && (
        <OrgMembersModal
          isOpen={isOrgMembersOpen}
          onClose={() => setIsOrgMembersOpen(false)}
          orgId={activeOrg.id}
          orgName={activeOrg.name}
          currentUserRole={user.role}
        />
      )}
    </div>
  );
}

import React, { useState } from 'react';
import { 
  Briefcase, 
  Building2, 
  Plus, 
  Bell, 
  LogOut, 
  CheckCheck, 
  Layers, 
  Users, 
  UserCheck 
} from 'lucide-react';

export default function Navbar({
  user,
  onLogout,
  organizations,
  activeOrg,
  onSelectOrg,
  onOpenCreateOrg,
  projects,
  activeProject,
  onSelectProject,
  onOpenCreateProject,
  onOpenOrgMembers,
  notifications,
  unreadCount,
  onMarkNotificationRead,
  onMarkAllNotificationsRead,
}) {
  const [showNotifications, setShowNotifications] = useState(false);
  const [showUserMenu, setShowUserMenu] = useState(false);

  return (
    <nav className="glass-panel" style={{ 
      margin: '0.75rem 1rem', 
      padding: '0.65rem 1.25rem', 
      display: 'flex', 
      alignItems: 'center', 
      justifyContent: 'space-between',
      position: 'sticky',
      top: '0.75rem',
      zIndex: 50
    }}>
      {/* Brand & Context Switcher */}
      <div style={{ display: 'flex', alignItems: 'center', gap: '1.25rem' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem' }}>
          <div style={{
            width: '34px',
            height: '34px',
            borderRadius: '9px',
            background: 'linear-gradient(135deg, #6366f1, #8b5cf6)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            boxShadow: '0 0 16px rgba(99, 102, 241, 0.4)'
          }}>
            <Layers size={20} color="#fff" />
          </div>
          <div>
            <span style={{ 
              fontFamily: 'var(--font-display)', 
              fontSize: '1.15rem', 
              fontWeight: 700, 
              background: 'linear-gradient(to right, #ffffff, #c7d2fe)', 
              WebkitBackgroundClip: 'text', 
              WebkitTextFillColor: 'transparent',
              letterSpacing: '-0.02em'
            }}>
              DevFlow
            </span>
          </div>
        </div>

        <div style={{ height: '22px', width: '1px', background: 'var(--border-subtle)' }} />

        {/* Organization Switcher */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <Building2 size={16} color="var(--text-muted)" />
          <select 
            id="org-select"
            value={activeOrg?.id || ''} 
            onChange={(e) => {
              if (e.target.value === 'NEW') {
                onOpenCreateOrg();
              } else {
                const org = organizations.find(o => o.id === Number(e.target.value));
                if (org) onSelectOrg(org);
              }
            }}
            style={{ 
              padding: '0.4rem 0.75rem', 
              fontSize: '0.85rem', 
              fontWeight: 500,
              width: 'auto',
              minWidth: '160px',
              cursor: 'pointer'
            }}
          >
            {organizations.length === 0 && <option value="">No Organizations</option>}
            {organizations.map(org => (
              <option key={org.id} value={org.id}>{org.name}</option>
            ))}
            <option value="NEW">+ Create New Organization</option>
          </select>

          {activeOrg && (
            <button 
              id="org-members-btn"
              onClick={onOpenOrgMembers} 
              className="btn-icon" 
              title="Organization Members"
              style={{ padding: '0.4rem' }}
            >
              <Users size={16} />
            </button>
          )}
        </div>

        {/* Project Switcher */}
        {activeOrg && (
          <>
            <div style={{ height: '22px', width: '1px', background: 'var(--border-subtle)' }} />
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <Briefcase size={16} color="var(--text-muted)" />
              <select
                id="project-select"
                value={activeProject?.id || ''}
                onChange={(e) => {
                  if (e.target.value === 'NEW') {
                    onOpenCreateProject();
                  } else {
                    const prj = projects.find(p => p.id === Number(e.target.value));
                    if (prj) onSelectProject(prj);
                  }
                }}
                style={{
                  padding: '0.4rem 0.75rem',
                  fontSize: '0.85rem',
                  fontWeight: 500,
                  width: 'auto',
                  minWidth: '160px',
                  cursor: 'pointer'
                }}
              >
                {projects.length === 0 && <option value="">No Projects</option>}
                {projects.map(p => (
                  <option key={p.id} value={p.id}>{p.name}</option>
                ))}
                <option value="NEW">+ Create New Project</option>
              </select>
            </div>
          </>
        )}
      </div>

      {/* Right Controls: Notifications & User profile */}
      <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
        {/* Notifications Bell */}
        <div style={{ position: 'relative' }}>
          <button 
            id="notifications-bell-btn"
            onClick={() => setShowNotifications(!showNotifications)}
            className="btn-icon"
            style={{ position: 'relative' }}
          >
            <Bell size={18} />
            {unreadCount > 0 && (
              <span style={{
                position: 'absolute',
                top: '2px',
                right: '2px',
                width: '18px',
                height: '18px',
                borderRadius: '50%',
                background: 'var(--accent-rose)',
                color: '#fff',
                fontSize: '0.65rem',
                fontWeight: 700,
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                boxShadow: '0 0 8px rgba(244, 63, 94, 0.6)'
              }}>
                {unreadCount > 9 ? '9+' : unreadCount}
              </span>
            )}
          </button>

          {/* Notifications Dropdown */}
          {showNotifications && (
            <div className="glass-panel animate-fade-in" style={{
              position: 'absolute',
              right: 0,
              top: 'calc(100% + 8px)',
              width: '340px',
              maxHeight: '420px',
              overflowY: 'auto',
              padding: '0.75rem',
              zIndex: 100,
              boxShadow: 'var(--shadow-lg)',
              background: 'rgba(15, 20, 30, 0.95)'
            }}>
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '0.75rem', paddingBottom: '0.5rem', borderBottom: '1px solid var(--border-subtle)' }}>
                <span style={{ fontWeight: 600, fontSize: '0.9rem' }}>Notifications</span>
                {unreadCount > 0 && (
                  <button 
                    onClick={onMarkAllNotificationsRead} 
                    style={{ fontSize: '0.75rem', color: 'var(--accent-primary)', display: 'flex', alignItems: 'center', gap: '0.25rem' }}
                  >
                    <CheckCheck size={14} /> Mark all read
                  </button>
                )}
              </div>

              {notifications.length === 0 ? (
                <div style={{ textAlign: 'center', padding: '1.5rem 0', color: 'var(--text-muted)', fontSize: '0.85rem' }}>
                  No notifications yet
                </div>
              ) : (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '0.4rem' }}>
                  {notifications.map(n => (
                    <div 
                      key={n.id}
                      onClick={() => !n.read && onMarkNotificationRead(n.id)}
                      style={{
                        padding: '0.6rem 0.75rem',
                        borderRadius: 'var(--radius-md)',
                        background: n.read ? 'transparent' : 'rgba(99, 102, 241, 0.08)',
                        border: n.read ? '1px solid transparent' : '1px solid rgba(99, 102, 241, 0.2)',
                        cursor: n.read ? 'default' : 'pointer',
                        transition: 'background var(--transition-fast)'
                      }}
                    >
                      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: '0.5rem' }}>
                        <span style={{ fontSize: '0.82rem', fontWeight: n.read ? 400 : 500, color: n.read ? 'var(--text-secondary)' : 'var(--text-primary)' }}>
                          {n.message}
                        </span>
                        {!n.read && (
                          <div style={{ width: '7px', height: '7px', borderRadius: '50%', background: 'var(--accent-primary)', marginTop: '4px', flexShrink: 0 }} />
                        )}
                      </div>
                      <span style={{ fontSize: '0.7rem', color: 'var(--text-muted)', marginTop: '0.25rem', display: 'block' }}>
                        {new Date(n.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                      </span>
                    </div>
                  ))}
                </div>
              )}
            </div>
          )}
        </div>

        {/* User Profile */}
        <div style={{ position: 'relative' }}>
          <button 
            id="user-profile-menu-btn"
            onClick={() => setShowUserMenu(!showUserMenu)}
            style={{ 
              display: 'flex', 
              alignItems: 'center', 
              gap: '0.6rem',
              padding: '0.3rem 0.6rem',
              borderRadius: 'var(--radius-md)',
              background: 'rgba(255, 255, 255, 0.04)',
              border: '1px solid var(--border-subtle)'
            }}
          >
            <div style={{
              width: '26px',
              height: '26px',
              borderRadius: '50%',
              background: 'linear-gradient(135deg, #4f46e5, #06b6d4)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              fontSize: '0.75rem',
              fontWeight: 700,
              color: '#fff'
            }}>
              {user.email ? user.email.charAt(0).toUpperCase() : 'U'}
            </div>
            <div style={{ textAlign: 'left', display: 'flex', flexDirection: 'column' }}>
              <span style={{ fontSize: '0.8rem', fontWeight: 500, color: 'var(--text-primary)', maxWidth: '140px', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                {user.email}
              </span>
              <span className="badge badge-role" style={{ fontSize: '0.6rem', padding: '0.05rem 0.35rem', alignSelf: 'flex-start' }}>
                {user.role}
              </span>
            </div>
          </button>

          {showUserMenu && (
            <div className="glass-panel animate-fade-in" style={{
              position: 'absolute',
              right: 0,
              top: 'calc(100% + 8px)',
              width: '180px',
              padding: '0.5rem',
              zIndex: 100,
              boxShadow: 'var(--shadow-lg)',
              background: 'rgba(15, 20, 30, 0.95)'
            }}>
              <button 
                id="logout-btn"
                onClick={onLogout}
                style={{ 
                  width: '100%', 
                  display: 'flex', 
                  alignItems: 'center', 
                  gap: '0.5rem', 
                  padding: '0.5rem 0.65rem',
                  color: 'var(--accent-rose)',
                  fontSize: '0.85rem',
                  borderRadius: 'var(--radius-sm)',
                  textAlign: 'left'
                }}
              >
                <LogOut size={16} /> Sign Out
              </button>
            </div>
          )}
        </div>
      </div>
    </nav>
  );
}

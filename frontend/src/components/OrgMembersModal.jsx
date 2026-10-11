import React, { useState, useEffect } from 'react';
import { X, Users, UserPlus, Trash2, Shield } from 'lucide-react';
import { api } from '../api/client';

export default function OrgMembersModal({ isOpen, onClose, orgId, orgName, currentUserRole }) {
  const [members, setMembers] = useState([]);
  const [loading, setLoading] = useState(false);
  const [inviteEmail, setInviteEmail] = useState('');
  const [inviteRole, setInviteRole] = useState('DEVELOPER');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  useEffect(() => {
    if (isOpen && orgId) {
      fetchMembers();
    }
  }, [isOpen, orgId]);

  const fetchMembers = async () => {
    setLoading(true);
    try {
      const data = await api.organizations.get(orgId);
      // OrganizationResponse includes members list
      setMembers(data.members || []);
    } catch (err) {
      console.error('Failed to load organization members', err);
    } finally {
      setLoading(false);
    }
  };

  const handleAddMember = async (e) => {
    e.preventDefault();
    if (!inviteEmail.trim()) return;

    setSubmitting(true);
    setError('');
    setSuccess('');

    try {
      const newMember = await api.organizations.addMember(orgId, inviteEmail.trim(), inviteRole);
      setMembers([...members, newMember]);
      setSuccess(`Added ${inviteEmail} as ${inviteRole}`);
      setInviteEmail('');
    } catch (err) {
      setError(err.message || 'Failed to add member to organization');
    } finally {
      setSubmitting(false);
    }
  };

  const handleRemoveMember = async (userId, userEmail) => {
    if (!window.confirm(`Are you sure you want to remove ${userEmail}?`)) return;

    try {
      await api.organizations.removeMember(orgId, userId);
      setMembers(members.filter(m => m.userId !== userId));
    } catch (err) {
      alert(err.message || 'Failed to remove member');
    }
  };

  if (!isOpen) return null;

  return (
    <div style={{
      position: 'fixed',
      inset: 0,
      background: 'rgba(5, 7, 12, 0.75)',
      backdropFilter: 'blur(10px)',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      padding: '1.5rem',
      zIndex: 250
    }}>
      <div className="glass-panel animate-fade-in" style={{
        width: '100%',
        maxWidth: '560px',
        maxHeight: '85vh',
        display: 'flex',
        flexDirection: 'column',
        boxShadow: 'var(--shadow-lg)',
        background: 'rgba(15, 20, 30, 0.95)',
        border: '1px solid rgba(255, 255, 255, 0.12)',
        overflow: 'hidden'
      }}>
        {/* Header */}
        <div style={{
          padding: '1.25rem 1.5rem',
          borderBottom: '1px solid var(--border-subtle)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between'
        }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem' }}>
            <Users size={20} color="var(--accent-primary)" />
            <div>
              <h3 style={{ fontSize: '1.15rem', fontWeight: 600 }}>{orgName} Members</h3>
              <p style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Manage team access and permissions</p>
            </div>
          </div>
          <button onClick={onClose} className="btn-icon">
            <X size={18} />
          </button>
        </div>

        <div style={{ padding: '1.5rem', overflowY: 'auto', display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
          {/* Add member form */}
          <form onSubmit={handleAddMember} style={{
            background: 'rgba(255, 255, 255, 0.03)',
            padding: '1rem',
            borderRadius: 'var(--radius-md)',
            border: '1px solid var(--border-subtle)',
            display: 'flex',
            flexDirection: 'column',
            gap: '0.75rem'
          }}>
            <span style={{ fontSize: '0.85rem', fontWeight: 600, display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
              <UserPlus size={16} color="var(--accent-primary)" /> Invite New Member
            </span>

            {error && (
              <div style={{ background: 'rgba(244, 63, 94, 0.12)', border: '1px solid rgba(244, 63, 94, 0.3)', color: '#fb7185', padding: '0.5rem', borderRadius: 'var(--radius-sm)', fontSize: '0.78rem' }}>
                {error}
              </div>
            )}
            {success && (
              <div style={{ background: 'rgba(16, 185, 129, 0.12)', border: '1px solid rgba(16, 185, 129, 0.3)', color: '#34d399', padding: '0.5rem', borderRadius: 'var(--radius-sm)', fontSize: '0.78rem' }}>
                {success}
              </div>
            )}

            <div style={{ display: 'grid', gridTemplateColumns: '1.4fr 1fr auto', gap: '0.5rem' }}>
              <input 
                id="invite-member-email-input"
                type="email" 
                required 
                placeholder="colleague@company.com" 
                value={inviteEmail} 
                onChange={(e) => setInviteEmail(e.target.value)}
              />
              <select 
                id="invite-member-role-select"
                value={inviteRole} 
                onChange={(e) => setInviteRole(e.target.value)}
              >
                <option value="DEVELOPER">Developer</option>
                <option value="PROJECT_MANAGER">Project Manager</option>
                <option value="ORGANIZATION_ADMIN">Admin</option>
                <option value="VIEWER">Viewer</option>
              </select>
              <button 
                type="submit" 
                id="invite-member-submit-btn"
                disabled={submitting} 
                className="btn-primary" 
                style={{ padding: '0.6rem 0.9rem' }}
              >
                Add
              </button>
            </div>
          </form>

          {/* Members list */}
          <div>
            <span style={{ fontSize: '0.8rem', fontWeight: 600, color: 'var(--text-muted)', textTransform: 'uppercase', display: 'block', marginBottom: '0.5rem' }}>
              Active Members ({members.length})
            </span>

            {loading ? (
              <div style={{ textAlign: 'center', padding: '1.5rem', color: 'var(--text-muted)' }}>Loading members...</div>
            ) : members.length === 0 ? (
              <div style={{ textAlign: 'center', padding: '1.5rem', color: 'var(--text-muted)' }}>No members listed</div>
            ) : (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                {members.map(m => (
                  <div key={m.userId} style={{
                    padding: '0.65rem 0.85rem',
                    borderRadius: 'var(--radius-md)',
                    background: 'rgba(255, 255, 255, 0.02)',
                    border: '1px solid var(--border-subtle)',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'space-between'
                  }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '0.65rem' }}>
                      <div style={{
                        width: '28px',
                        height: '28px',
                        borderRadius: '50%',
                        background: 'linear-gradient(135deg, #6366f1, #8b5cf6)',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        color: '#fff',
                        fontSize: '0.75rem',
                        fontWeight: 700
                      }}>
                        {m.userEmail ? m.userEmail.charAt(0).toUpperCase() : 'U'}
                      </div>
                      <div>
                        <div style={{ fontSize: '0.85rem', fontWeight: 500, color: 'var(--text-primary)' }}>
                          {m.userEmail}
                        </div>
                        <div style={{ fontSize: '0.7rem', color: 'var(--text-muted)' }}>
                          Joined {m.joinedAt ? new Date(m.joinedAt).toLocaleDateString() : 'Recently'}
                        </div>
                      </div>
                    </div>

                    <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                      <span className="badge badge-role" style={{ fontSize: '0.68rem' }}>
                        {m.role}
                      </span>
                      {m.role !== 'ORGANIZATION_OWNER' && (
                        <button 
                          onClick={() => handleRemoveMember(m.userId, m.userEmail)}
                          className="btn-icon" 
                          title="Remove member"
                          style={{ color: 'var(--accent-rose)', padding: '0.35rem' }}
                        >
                          <Trash2 size={15} />
                        </button>
                      )}
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

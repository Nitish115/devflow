import React from 'react';
import { Calendar, User, Clock, CheckCircle2 } from 'lucide-react';

export default function TaskCard({ task, onClick, onQuickStatusChange }) {
  const getPriorityBadgeClass = (priority) => {
    switch (priority) {
      case 'URGENT': return 'badge-urgent';
      case 'HIGH': return 'badge-high';
      case 'MEDIUM': return 'badge-medium';
      case 'LOW': return 'badge-low';
      default: return 'badge-medium';
    }
  };

  const isOverdue = task.dueDate && new Date(task.dueDate) < new Date() && task.status !== 'DONE';

  return (
    <div 
      className="glass-panel"
      onClick={() => onClick(task)}
      style={{
        padding: '1rem',
        cursor: 'pointer',
        transition: 'all var(--transition-fast)',
        border: '1px solid var(--border-card)',
        position: 'relative'
      }}
      onMouseEnter={(e) => {
        e.currentTarget.style.borderColor = 'rgba(99, 102, 241, 0.4)';
        e.currentTarget.style.transform = 'translateY(-2px)';
        e.currentTarget.style.boxShadow = 'var(--shadow-md)';
      }}
      onMouseLeave={(e) => {
        e.currentTarget.style.borderColor = 'var(--border-card)';
        e.currentTarget.style.transform = 'translateY(0)';
        e.currentTarget.style.boxShadow = 'none';
      }}
    >
      {/* Top row: Priority & Status indicator */}
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '0.65rem' }}>
        <span className={`badge ${getPriorityBadgeClass(task.priority)}`}>
          {task.priority || 'MEDIUM'}
        </span>

        {task.status === 'DONE' && (
          <CheckCircle2 size={16} color="var(--accent-emerald)" />
        )}
      </div>

      {/* Title */}
      <h4 style={{ 
        fontSize: '0.95rem', 
        fontWeight: 600, 
        color: 'var(--text-primary)', 
        marginBottom: '0.4rem',
        lineHeight: 1.35
      }}>
        {task.title}
      </h4>

      {/* Description preview */}
      {task.description && (
        <p style={{ 
          fontSize: '0.8rem', 
          color: 'var(--text-muted)', 
          marginBottom: '0.85rem',
          display: '-webkit-box',
          WebkitLineClamp: 2,
          WebkitBoxOrient: 'vertical',
          overflow: 'hidden',
          lineHeight: 1.45
        }}>
          {task.description}
        </p>
      )}

      {/* Footer row: Due date & Assignee */}
      <div style={{ 
        display: 'flex', 
        alignItems: 'center', 
        justifyContent: 'space-between', 
        paddingTop: '0.65rem',
        borderTop: '1px solid var(--border-subtle)',
        fontSize: '0.75rem',
        color: 'var(--text-secondary)'
      }}>
        {task.dueDate ? (
          <div style={{ 
            display: 'flex', 
            alignItems: 'center', 
            gap: '0.35rem',
            color: isOverdue ? 'var(--accent-rose)' : 'var(--text-muted)'
          }}>
            <Calendar size={13} />
            <span>{task.dueDate}</span>
          </div>
        ) : (
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.35rem', color: 'var(--text-muted)' }}>
            <Clock size={13} />
            <span>No date</span>
          </div>
        )}

        {task.assigneeEmail ? (
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.35rem' }} title={`Assigned to ${task.assigneeEmail}`}>
            <div style={{
              width: '22px',
              height: '22px',
              borderRadius: '50%',
              background: 'linear-gradient(135deg, #6366f1, #06b6d4)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              color: '#fff',
              fontSize: '0.65rem',
              fontWeight: 700
            }}>
              {task.assigneeEmail.charAt(0).toUpperCase()}
            </div>
            <span style={{ maxWidth: '90px', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
              {task.assigneeEmail.split('@')[0]}
            </span>
          </div>
        ) : (
          <span style={{ color: 'var(--text-muted)', fontSize: '0.72rem', fontStyle: 'italic' }}>
            Unassigned
          </span>
        )}
      </div>
    </div>
  );
}

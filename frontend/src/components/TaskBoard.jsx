import React, { useState } from 'react';
import { Plus, Search, Filter, LayoutGrid, ListFilter, AlertCircle } from 'lucide-react';
import TaskCard from './TaskCard';

const COLUMNS = [
  { id: 'TODO', label: 'To Do', color: '#38bdf8' },
  { id: 'IN_PROGRESS', label: 'In Progress', color: '#fbbf24' },
  { id: 'IN_REVIEW', label: 'In Review', color: '#c084fc' },
  { id: 'DONE', label: 'Completed', color: '#34d399' },
];

export default function TaskBoard({ 
  tasks, 
  onTaskClick, 
  onOpenCreateTask, 
  project,
  orgMembers,
  onQuickStatusChange
}) {
  const [searchQuery, setSearchQuery] = useState('');
  const [priorityFilter, setPriorityFilter] = useState('ALL');
  const [assigneeFilter, setAssigneeFilter] = useState('ALL');

  // Filter tasks
  const filteredTasks = tasks.filter(task => {
    const matchesSearch = task.title.toLowerCase().includes(searchQuery.toLowerCase()) ||
      (task.description && task.description.toLowerCase().includes(searchQuery.toLowerCase()));
    const matchesPriority = priorityFilter === 'ALL' || task.priority === priorityFilter;
    const matchesAssignee = assigneeFilter === 'ALL' || (task.assigneeId && task.assigneeId.toString() === assigneeFilter);
    return matchesSearch && matchesPriority && matchesAssignee;
  });

  return (
    <div style={{ padding: '0.5rem 1rem 2rem 1rem', display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
      {/* Project Banner & Action Bar */}
      <div className="glass-panel" style={{ padding: '1.25rem 1.5rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '1rem' }}>
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.65rem' }}>
            <h2 style={{ fontSize: '1.4rem', fontWeight: 700 }}>{project.name}</h2>
            <span className="badge" style={{ background: 'rgba(255, 255, 255, 0.08)', color: 'var(--text-secondary)' }}>
              {project.status}
            </span>
          </div>
          {project.description && (
            <p style={{ color: 'var(--text-secondary)', fontSize: '0.875rem', marginTop: '0.25rem' }}>
              {project.description}
            </p>
          )}
        </div>

        <button 
          id="create-task-main-btn"
          onClick={onOpenCreateTask} 
          className="btn-primary"
        >
          <Plus size={16} /> New Task
        </button>
      </div>

      {/* Filter Toolbar */}
      <div style={{ display: 'flex', gap: '0.75rem', flexWrap: 'wrap', alignItems: 'center', justifyContent: 'space-between' }}>
        <div style={{ display: 'flex', gap: '0.75rem', flexWrap: 'wrap', alignItems: 'center' }}>
          {/* Search bar */}
          <div style={{ position: 'relative', width: '240px' }}>
            <input 
              id="task-search-input"
              type="text" 
              placeholder="Search tasks..." 
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              style={{ paddingLeft: '2.2rem', paddingRight: '0.75rem' }}
            />
            <Search size={15} color="var(--text-muted)" style={{ position: 'absolute', left: '0.8rem', top: '50%', transform: 'translateY(-50%)' }} />
          </div>

          {/* Priority filter */}
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
            <Filter size={15} color="var(--text-muted)" />
            <select 
              id="priority-filter-select"
              value={priorityFilter} 
              onChange={(e) => setPriorityFilter(e.target.value)}
              style={{ width: 'auto', padding: '0.55rem 0.8rem' }}
            >
              <option value="ALL">All Priorities</option>
              <option value="URGENT">Urgent</option>
              <option value="HIGH">High</option>
              <option value="MEDIUM">Medium</option>
              <option value="LOW">Low</option>
            </select>
          </div>

          {/* Assignee filter */}
          {orgMembers && orgMembers.length > 0 && (
            <select 
              id="assignee-filter-select"
              value={assigneeFilter} 
              onChange={(e) => setAssigneeFilter(e.target.value)}
              style={{ width: 'auto', padding: '0.55rem 0.8rem' }}
            >
              <option value="ALL">All Assignees</option>
              {orgMembers.map(m => (
                <option key={m.userId} value={m.userId}>{m.userEmail}</option>
              ))}
            </select>
          )}
        </div>

        <div style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>
          Showing <strong>{filteredTasks.length}</strong> of {tasks.length} tasks
        </div>
      </div>

      {/* Kanban Board Columns */}
      <div style={{
        display: 'grid',
        gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))',
        gap: '1.25rem',
        alignItems: 'start'
      }}>
        {COLUMNS.map(col => {
          const colTasks = filteredTasks.filter(t => t.status === col.id);
          return (
            <div 
              key={col.id} 
              className="glass-panel" 
              style={{ 
                padding: '1rem', 
                background: 'rgba(15, 20, 30, 0.65)',
                display: 'flex',
                flexDirection: 'column',
                gap: '0.85rem',
                minHeight: '380px'
              }}
            >
              {/* Column Header */}
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', paddingBottom: '0.5rem', borderBottom: '1px solid var(--border-subtle)' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                  <div style={{ width: '9px', height: '9px', borderRadius: '50%', background: col.color }} />
                  <span style={{ fontWeight: 600, fontSize: '0.9rem', color: 'var(--text-primary)' }}>
                    {col.label}
                  </span>
                  <span style={{ 
                    fontSize: '0.72rem', 
                    padding: '0.1rem 0.45rem', 
                    borderRadius: 'var(--radius-full)', 
                    background: 'rgba(255, 255, 255, 0.08)',
                    color: 'var(--text-secondary)'
                  }}>
                    {colTasks.length}
                  </span>
                </div>

                <button 
                  onClick={() => onOpenCreateTask(col.id)} 
                  className="btn-icon" 
                  title={`Add task to ${col.label}`}
                  style={{ padding: '0.25rem' }}
                >
                  <Plus size={16} />
                </button>
              </div>

              {/* Task list in column */}
              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
                {colTasks.length === 0 ? (
                  <div style={{ 
                    padding: '2rem 1rem', 
                    textAlign: 'center', 
                    color: 'var(--text-muted)', 
                    fontSize: '0.8rem',
                    border: '1px dashed var(--border-subtle)',
                    borderRadius: 'var(--radius-md)'
                  }}>
                    No tasks in {col.label.toLowerCase()}
                  </div>
                ) : (
                  colTasks.map(task => (
                    <TaskCard 
                      key={task.id} 
                      task={task} 
                      onClick={onTaskClick}
                      onQuickStatusChange={onQuickStatusChange}
                    />
                  ))
                )}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}

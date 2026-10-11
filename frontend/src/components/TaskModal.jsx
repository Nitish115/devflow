import React, { useState, useEffect } from 'react';
import { 
  X, 
  Trash2, 
  MessageSquare, 
  Paperclip, 
  Upload, 
  Download, 
  Send, 
  Clock, 
  Calendar, 
  User, 
  Save 
} from 'lucide-react';
import { api } from '../api/client';

export default function TaskModal({
  task,
  onClose,
  onTaskUpdated,
  onTaskDeleted,
  orgId,
  projectId,
  orgMembers
}) {
  const [title, setTitle] = useState(task.title || '');
  const [description, setDescription] = useState(task.description || '');
  const [status, setStatus] = useState(task.status || 'TODO');
  const [priority, setPriority] = useState(task.priority || 'MEDIUM');
  const [assigneeId, setAssigneeId] = useState(task.assigneeId || '');
  const [dueDate, setDueDate] = useState(task.dueDate || '');

  const [comments, setComments] = useState([]);
  const [newComment, setNewComment] = useState('');
  const [attachments, setAttachments] = useState([]);
  const [loadingComments, setLoadingComments] = useState(false);
  const [uploadingFile, setUploadingFile] = useState(false);
  const [isSaving, setIsSaving] = useState(false);
  const [activeTab, setActiveTab] = useState('comments'); // 'comments' | 'attachments'

  useEffect(() => {
    fetchComments();
    fetchAttachments();
  }, [task.id]);

  const fetchComments = async () => {
    setLoadingComments(true);
    try {
      const data = await api.comments.list(orgId, projectId, task.id);
      setComments(data || []);
    } catch (err) {
      console.error('Failed to load comments', err);
    } finally {
      setLoadingComments(false);
    }
  };

  const fetchAttachments = async () => {
    try {
      const data = await api.attachments.list(orgId, projectId, task.id);
      setAttachments(data || []);
    } catch (err) {
      console.error('Failed to load attachments', err);
    }
  };

  const handleSaveDetails = async () => {
    setIsSaving(true);
    try {
      const updated = await api.tasks.update(orgId, projectId, task.id, {
        title,
        description,
        status,
        priority,
        assigneeId: assigneeId ? Number(assigneeId) : null,
        dueDate: dueDate || null
      });
      onTaskUpdated(updated);
    } catch (err) {
      alert(err.message || 'Failed to update task');
    } finally {
      setIsSaving(false);
    }
  };

  const handleDelete = async () => {
    if (!window.confirm('Are you sure you want to delete this task?')) return;
    try {
      await api.tasks.delete(orgId, projectId, task.id);
      onTaskDeleted(task.id);
    } catch (err) {
      alert(err.message || 'Failed to delete task');
    }
  };

  const handleAddComment = async (e) => {
    e.preventDefault();
    if (!newComment.trim()) return;

    try {
      const comment = await api.comments.create(orgId, projectId, task.id, newComment);
      setComments([...comments, comment]);
      setNewComment('');
    } catch (err) {
      alert(err.message || 'Failed to post comment');
    }
  };

  const handleFileUpload = async (e) => {
    const file = e.target.files[0];
    if (!file) return;

    setUploadingFile(true);
    try {
      const uploaded = await api.attachments.upload(orgId, projectId, task.id, file);
      setAttachments([...attachments, uploaded]);
    } catch (err) {
      alert(err.message || 'Failed to upload attachment');
    } finally {
      setUploadingFile(false);
      e.target.value = '';
    }
  };

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
      zIndex: 200
    }}>
      <div className="glass-panel animate-fade-in" style={{
        width: '100%',
        maxWidth: '850px',
        maxHeight: '90vh',
        display: 'flex',
        flexDirection: 'column',
        boxShadow: 'var(--shadow-lg)',
        background: 'rgba(15, 20, 30, 0.95)',
        border: '1px solid rgba(255, 255, 255, 0.12)',
        overflow: 'hidden'
      }}>
        {/* Top Header */}
        <div style={{
          padding: '1.25rem 1.5rem',
          borderBottom: '1px solid var(--border-subtle)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between'
        }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
            <span style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>Task #{task.id}</span>
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <button 
              id="delete-task-btn"
              onClick={handleDelete} 
              className="btn-icon" 
              title="Delete Task" 
              style={{ color: 'var(--accent-rose)' }}
            >
              <Trash2 size={18} />
            </button>
            <button 
              id="close-task-modal-btn"
              onClick={onClose} 
              className="btn-icon"
            >
              <X size={20} />
            </button>
          </div>
        </div>

        {/* Modal Body: Left details, Right tabs (comments & attachments) */}
        <div style={{
          display: 'grid',
          gridTemplateColumns: '1.2fr 1fr',
          overflowY: 'auto',
          flex: 1
        }}>
          {/* Left Column: Task Core Info */}
          <div style={{ padding: '1.5rem', borderRight: '1px solid var(--border-subtle)', display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
            <div>
              <label style={{ display: 'block', fontSize: '0.75rem', fontWeight: 600, color: 'var(--text-muted)', textTransform: 'uppercase', marginBottom: '0.35rem' }}>
                Task Title
              </label>
              <input 
                id="edit-task-title-input"
                type="text" 
                value={title} 
                onChange={(e) => setTitle(e.target.value)} 
                style={{ fontSize: '1.1rem', fontWeight: 600 }}
              />
            </div>

            <div>
              <label style={{ display: 'block', fontSize: '0.75rem', fontWeight: 600, color: 'var(--text-muted)', textTransform: 'uppercase', marginBottom: '0.35rem' }}>
                Description
              </label>
              <textarea 
                id="edit-task-desc-input"
                rows={4}
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                placeholder="Add more details about this task..."
                style={{ resize: 'vertical' }}
              />
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
              <div>
                <label style={{ display: 'block', fontSize: '0.75rem', fontWeight: 600, color: 'var(--text-muted)', textTransform: 'uppercase', marginBottom: '0.35rem' }}>
                  Status
                </label>
                <select 
                  id="edit-task-status-select"
                  value={status} 
                  onChange={(e) => setStatus(e.target.value)}
                >
                  <option value="TODO">To Do</option>
                  <option value="IN_PROGRESS">In Progress</option>
                  <option value="IN_REVIEW">In Review</option>
                  <option value="DONE">Completed</option>
                </select>
              </div>

              <div>
                <label style={{ display: 'block', fontSize: '0.75rem', fontWeight: 600, color: 'var(--text-muted)', textTransform: 'uppercase', marginBottom: '0.35rem' }}>
                  Priority
                </label>
                <select 
                  id="edit-task-priority-select"
                  value={priority} 
                  onChange={(e) => setPriority(e.target.value)}
                >
                  <option value="LOW">Low</option>
                  <option value="MEDIUM">Medium</option>
                  <option value="HIGH">High</option>
                  <option value="URGENT">Urgent</option>
                </select>
              </div>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
              <div>
                <label style={{ display: 'block', fontSize: '0.75rem', fontWeight: 600, color: 'var(--text-muted)', textTransform: 'uppercase', marginBottom: '0.35rem' }}>
                  Assignee
                </label>
                <select 
                  id="edit-task-assignee-select"
                  value={assigneeId} 
                  onChange={(e) => setAssigneeId(e.target.value)}
                >
                  <option value="">Unassigned</option>
                  {orgMembers && orgMembers.map(m => (
                    <option key={m.userId} value={m.userId}>{m.userEmail}</option>
                  ))}
                </select>
              </div>

              <div>
                <label style={{ display: 'block', fontSize: '0.75rem', fontWeight: 600, color: 'var(--text-muted)', textTransform: 'uppercase', marginBottom: '0.35rem' }}>
                  Due Date
                </label>
                <input 
                  id="edit-task-duedate-input"
                  type="date" 
                  value={dueDate} 
                  onChange={(e) => setDueDate(e.target.value)}
                />
              </div>
            </div>

            <button 
              id="save-task-changes-btn"
              onClick={handleSaveDetails} 
              disabled={isSaving}
              className="btn-primary" 
              style={{ alignSelf: 'flex-start', marginTop: 'auto' }}
            >
              <Save size={16} /> {isSaving ? 'Saving...' : 'Save Changes'}
            </button>
          </div>

          {/* Right Column: Comments & Attachments */}
          <div style={{ display: 'flex', flexDirection: 'column', background: 'rgba(10, 13, 20, 0.4)' }}>
            {/* Tab switch */}
            <div style={{ display: 'flex', borderBottom: '1px solid var(--border-subtle)' }}>
              <button 
                onClick={() => setActiveTab('comments')}
                style={{
                  flex: 1,
                  padding: '0.85rem',
                  fontSize: '0.85rem',
                  fontWeight: 600,
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  gap: '0.4rem',
                  borderBottom: activeTab === 'comments' ? '2px solid var(--accent-primary)' : '2px solid transparent',
                  color: activeTab === 'comments' ? 'var(--text-primary)' : 'var(--text-muted)'
                }}
              >
                <MessageSquare size={15} /> Comments ({comments.length})
              </button>
              <button 
                onClick={() => setActiveTab('attachments')}
                style={{
                  flex: 1,
                  padding: '0.85rem',
                  fontSize: '0.85rem',
                  fontWeight: 600,
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  gap: '0.4rem',
                  borderBottom: activeTab === 'attachments' ? '2px solid var(--accent-primary)' : '2px solid transparent',
                  color: activeTab === 'attachments' ? 'var(--text-primary)' : 'var(--text-muted)'
                }}
              >
                <Paperclip size={15} /> Files ({attachments.length})
              </button>
            </div>

            {/* Tab content */}
            <div style={{ flex: 1, overflowY: 'auto', padding: '1rem', display: 'flex', flexDirection: 'column' }}>
              {activeTab === 'comments' ? (
                <>
                  <div style={{ flex: 1, display: 'flex', flexDirection: 'column', gap: '0.75rem', marginBottom: '1rem' }}>
                    {loadingComments ? (
                      <div style={{ textAlign: 'center', color: 'var(--text-muted)', padding: '2rem 0' }}>Loading comments...</div>
                    ) : comments.length === 0 ? (
                      <div style={{ textAlign: 'center', color: 'var(--text-muted)', padding: '2rem 0', fontSize: '0.85rem' }}>
                        No comments yet. Start the conversation!
                      </div>
                    ) : (
                      comments.map(c => (
                        <div key={c.id} style={{
                          padding: '0.75rem',
                          borderRadius: 'var(--radius-md)',
                          background: 'rgba(255, 255, 255, 0.03)',
                          border: '1px solid var(--border-subtle)'
                        }}>
                          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '0.35rem' }}>
                            <span style={{ fontSize: '0.78rem', fontWeight: 600, color: 'var(--accent-primary)' }}>
                              {c.userEmail || 'Member'}
                            </span>
                            <span style={{ fontSize: '0.7rem', color: 'var(--text-muted)' }}>
                              {new Date(c.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                            </span>
                          </div>
                          <p style={{ fontSize: '0.85rem', color: 'var(--text-primary)', whiteSpace: 'pre-wrap' }}>
                            {c.content}
                          </p>
                        </div>
                      ))
                    )}
                  </div>

                  {/* Add comment input */}
                  <form onSubmit={handleAddComment} style={{ display: 'flex', gap: '0.5rem' }}>
                    <input 
                      id="new-comment-input"
                      type="text" 
                      placeholder="Write a comment (@ to mention)..." 
                      value={newComment} 
                      onChange={(e) => setNewComment(e.target.value)} 
                    />
                    <button type="submit" className="btn-primary" style={{ padding: '0.65rem 0.9rem' }}>
                      <Send size={15} />
                    </button>
                  </form>
                </>
              ) : (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                  {/* File upload button */}
                  <div>
                    <label 
                      className="btn-secondary" 
                      style={{ width: '100%', cursor: 'pointer', borderStyle: 'dashed' }}
                    >
                      <Upload size={16} /> {uploadingFile ? 'Uploading...' : 'Upload File Attachment'}
                      <input 
                        type="file" 
                        onChange={handleFileUpload} 
                        style={{ display: 'none' }} 
                        disabled={uploadingFile}
                      />
                    </label>
                  </div>

                  {/* Attachments list */}
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                    {attachments.length === 0 ? (
                      <div style={{ textAlign: 'center', color: 'var(--text-muted)', padding: '2rem 0', fontSize: '0.85rem' }}>
                        No files attached yet.
                      </div>
                    ) : (
                      attachments.map(att => (
                        <div key={att.id} style={{
                          padding: '0.65rem 0.85rem',
                          borderRadius: 'var(--radius-md)',
                          background: 'rgba(255, 255, 255, 0.03)',
                          border: '1px solid var(--border-subtle)',
                          display: 'flex',
                          alignItems: 'center',
                          justifyContent: 'space-between'
                        }}>
                          <div>
                            <div style={{ fontSize: '0.85rem', fontWeight: 500, color: 'var(--text-primary)' }}>
                              {att.filename}
                            </div>
                            <div style={{ fontSize: '0.72rem', color: 'var(--text-muted)' }}>
                              {(att.fileSize / 1024).toFixed(1)} KB • by {att.uploadedByEmail || 'Member'}
                            </div>
                          </div>

                          <a 
                            href={api.attachments.downloadUrl(orgId, projectId, task.id, att.id)}
                            target="_blank"
                            rel="noopener noreferrer"
                            className="btn-icon"
                            title="Download file"
                          >
                            <Download size={16} />
                          </a>
                        </div>
                      ))
                    )}
                  </div>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

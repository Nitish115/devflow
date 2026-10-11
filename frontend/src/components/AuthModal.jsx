import React, { useState } from 'react';
import { Layers, Mail, Lock, Shield, ArrowRight, Sparkles } from 'lucide-react';
import { api } from '../api/client';

export default function AuthModal({ onAuthSuccess }) {
  const [isRegister, setIsRegister] = useState(false);
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [role, setRole] = useState('ORGANIZATION_OWNER');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      if (isRegister) {
        await api.auth.register(email, password, role);
        // Automatically login after successful registration
        const authData = await api.auth.login(email, password);
        onAuthSuccess(authData);
      } else {
        const authData = await api.auth.login(email, password);
        onAuthSuccess(authData);
      }
    } catch (err) {
      setError(err.message || 'Authentication failed');
    } finally {
      setLoading(false);
    }
  };

  const handleDemoLogin = async () => {
    setEmail('demo.owner@devflow.local');
    setPassword('Password123!');
    setRole('ORGANIZATION_OWNER');
    setError('');
    setLoading(true);
    try {
      try {
        const authData = await api.auth.login('demo.owner@devflow.local', 'Password123!');
        onAuthSuccess(authData);
      } catch (loginErr) {
        // If demo user doesn't exist yet, register first then login
        await api.auth.register('demo.owner@devflow.local', 'Password123!', 'ORGANIZATION_OWNER');
        const authData = await api.auth.login('demo.owner@devflow.local', 'Password123!');
        onAuthSuccess(authData);
      }
    } catch (err) {
      setError(err.message || 'Failed to initialize demo account');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{
      position: 'fixed',
      inset: 0,
      background: 'rgba(5, 7, 12, 0.85)',
      backdropFilter: 'blur(20px)',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      padding: '1.5rem',
      zIndex: 1000
    }}>
      <div className="glass-panel animate-fade-in" style={{
        width: '100%',
        maxWidth: '430px',
        padding: '2.5rem',
        boxShadow: 'var(--shadow-lg), var(--shadow-glow)',
        border: '1px solid rgba(255, 255, 255, 0.12)'
      }}>
        {/* Brand Header */}
        <div style={{ textAlign: 'center', marginBottom: '2rem' }}>
          <div style={{
            width: '46px',
            height: '46px',
            borderRadius: '13px',
            background: 'linear-gradient(135deg, #6366f1, #8b5cf6)',
            display: 'inline-flex',
            alignItems: 'center',
            justifyContent: 'center',
            marginBottom: '0.9rem',
            boxShadow: '0 0 24px rgba(99, 102, 241, 0.5)'
          }}>
            <Layers size={26} color="#fff" />
          </div>
          <h2 style={{ fontSize: '1.6rem', fontWeight: 700, letterSpacing: '-0.02em', marginBottom: '0.35rem' }}>
            {isRegister ? 'Create your DevFlow account' : 'Welcome back to DevFlow'}
          </h2>
          <p style={{ color: 'var(--text-secondary)', fontSize: '0.875rem' }}>
            {isRegister 
              ? 'Join your team and coordinate enterprise projects'
              : 'Sign in to access your organizations and project boards'}
          </p>
        </div>

        {/* Tab switch */}
        <div style={{
          display: 'grid',
          gridTemplateColumns: '1fr 1fr',
          background: 'rgba(255, 255, 255, 0.04)',
          borderRadius: 'var(--radius-md)',
          padding: '0.25rem',
          marginBottom: '1.5rem',
          border: '1px solid var(--border-subtle)'
        }}>
          <button
            type="button"
            id="tab-signin-btn"
            onClick={() => { setIsRegister(false); setError(''); }}
            style={{
              padding: '0.5rem',
              fontSize: '0.85rem',
              fontWeight: 500,
              borderRadius: 'var(--radius-sm)',
              color: !isRegister ? '#fff' : 'var(--text-secondary)',
              background: !isRegister ? 'rgba(99, 102, 241, 0.25)' : 'transparent',
              border: !isRegister ? '1px solid rgba(99, 102, 241, 0.4)' : '1px solid transparent',
              transition: 'all var(--transition-fast)'
            }}
          >
            Sign In
          </button>
          <button
            type="button"
            id="tab-register-btn"
            onClick={() => { setIsRegister(true); setError(''); }}
            style={{
              padding: '0.5rem',
              fontSize: '0.85rem',
              fontWeight: 500,
              borderRadius: 'var(--radius-sm)',
              color: isRegister ? '#fff' : 'var(--text-secondary)',
              background: isRegister ? 'rgba(99, 102, 241, 0.25)' : 'transparent',
              border: isRegister ? '1px solid rgba(99, 102, 241, 0.4)' : '1px solid transparent',
              transition: 'all var(--transition-fast)'
            }}
          >
            Create Account
          </button>
        </div>

        {error && (
          <div style={{
            background: 'rgba(244, 63, 94, 0.12)',
            border: '1px solid rgba(244, 63, 94, 0.3)',
            color: '#fb7185',
            padding: '0.65rem 0.85rem',
            borderRadius: 'var(--radius-md)',
            fontSize: '0.82rem',
            marginBottom: '1.25rem'
          }}>
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
          <div>
            <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 500, color: 'var(--text-secondary)', marginBottom: '0.35rem' }}>
              Email Address
            </label>
            <div style={{ position: 'relative' }}>
              <input 
                id="auth-email-input"
                type="email" 
                required 
                placeholder="name@company.com" 
                value={email} 
                onChange={(e) => setEmail(e.target.value)} 
                style={{ paddingLeft: '2.4rem' }}
              />
              <Mail size={16} color="var(--text-muted)" style={{ position: 'absolute', left: '0.85rem', top: '50%', transform: 'translateY(-50%)' }} />
            </div>
          </div>

          <div>
            <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 500, color: 'var(--text-secondary)', marginBottom: '0.35rem' }}>
              Password
            </label>
            <div style={{ position: 'relative' }}>
              <input 
                id="auth-password-input"
                type="password" 
                required 
                placeholder="••••••••••••" 
                value={password} 
                onChange={(e) => setPassword(e.target.value)} 
                style={{ paddingLeft: '2.4rem' }}
              />
              <Lock size={16} color="var(--text-muted)" style={{ position: 'absolute', left: '0.85rem', top: '50%', transform: 'translateY(-50%)' }} />
            </div>
          </div>

          {isRegister && (
            <div>
              <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 500, color: 'var(--text-secondary)', marginBottom: '0.35rem' }}>
                Initial Organization Role
              </label>
              <div style={{ position: 'relative' }}>
                <select 
                  id="auth-role-select"
                  value={role} 
                  onChange={(e) => setRole(e.target.value)}
                  style={{ paddingLeft: '2.4rem' }}
                >
                  <option value="ORGANIZATION_OWNER">Organization Owner</option>
                  <option value="ORGANIZATION_ADMIN">Organization Admin</option>
                  <option value="PROJECT_MANAGER">Project Manager</option>
                  <option value="DEVELOPER">Developer</option>
                  <option value="VIEWER">Viewer</option>
                </select>
                <Shield size={16} color="var(--text-muted)" style={{ position: 'absolute', left: '0.85rem', top: '50%', transform: 'translateY(-50%)' }} />
              </div>
            </div>
          )}

          <button 
            type="submit" 
            id="auth-submit-btn"
            disabled={loading} 
            className="btn-primary" 
            style={{ width: '100%', marginTop: '0.5rem', padding: '0.75rem' }}
          >
            {loading ? 'Processing...' : (
              <>
                {isRegister ? 'Get Started' : 'Sign In to DevFlow'}
                <ArrowRight size={16} />
              </>
            )}
          </button>
        </form>

        {/* Demo Login Quick Action */}
        <div style={{ marginTop: '1.5rem', paddingTop: '1.25rem', borderTop: '1px solid var(--border-subtle)', textAlign: 'center' }}>
          <button 
            type="button"
            id="demo-account-btn"
            onClick={handleDemoLogin}
            disabled={loading}
            style={{ 
              fontSize: '0.8rem', 
              color: 'var(--text-secondary)', 
              display: 'inline-flex', 
              alignItems: 'center', 
              gap: '0.4rem',
              transition: 'color var(--transition-fast)'
            }}
            onMouseEnter={(e) => e.currentTarget.style.color = 'var(--text-primary)'}
            onMouseLeave={(e) => e.currentTarget.style.color = 'var(--text-secondary)'}
          >
            <Sparkles size={14} color="var(--accent-amber)" />
            Quick Demo: Sign in as Demo Owner
          </button>
        </div>
      </div>
    </div>
  );
}

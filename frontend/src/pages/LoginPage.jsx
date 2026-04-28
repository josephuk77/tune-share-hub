import { useState } from 'react'
import { Button } from '../components/common/Button.jsx'
import { useAuth } from '../hooks/useAuth.js'

export function LoginPage() {
  const { errorMessage, login, setErrorMessage } = useAuth()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)

  async function handleSubmit(event) {
    event.preventDefault()
    setIsSubmitting(true)
    setErrorMessage('')

    try {
      await login({ email, password })
    } catch (error) {
      setErrorMessage(error.message)
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <main className="login-page">
      <section className="login-panel" aria-labelledby="login-title">
        <div className="login-copy">
          <div className="brand-mark" aria-hidden="true">
            TS
          </div>
          <p className="eyebrow">Playlist Community</p>
          <h1 id="login-title">Tune Share Hub</h1>
          <p className="login-summary">
            사전 등록된 계정으로 플레이리스트를 만들고 공유합니다.
          </p>
        </div>

        <form className="login-form" onSubmit={handleSubmit}>
          <label>
            이메일
            <input
              autoComplete="email"
              name="email"
              onChange={(event) => setEmail(event.target.value)}
              required
              type="email"
              value={email}
            />
          </label>
          <label>
            비밀번호
            <input
              autoComplete="current-password"
              name="password"
              onChange={(event) => setPassword(event.target.value)}
              required
              type="password"
              value={password}
            />
          </label>
          {errorMessage ? <p className="form-error">{errorMessage}</p> : null}
          <Button className="button-primary" disabled={isSubmitting} type="submit">
            {isSubmitting ? '로그인 중' : '로그인'}
          </Button>
        </form>
      </section>
    </main>
  )
}

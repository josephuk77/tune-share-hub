export function EmptyState({ title, description }) {
  return (
    <section className="empty-state">
      <strong>{title}</strong>
      <p>{description}</p>
    </section>
  )
}

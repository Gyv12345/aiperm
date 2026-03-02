const escapeHtml = (input: string): string =>
  input
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;')

const parseInline = (input: string): string => {
  let text = input
  text = text.replace(/`([^`]+)`/g, '<code>$1</code>')
  text = text.replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>')
  text = text.replace(/\*([^*]+)\*/g, '<em>$1</em>')
  text = text.replace(/\[([^\]]+)\]\((https?:\/\/[^)]+)\)/g, '<a href="$2" target="_blank" rel="noopener noreferrer">$1</a>')
  return text
}

export const renderMarkdown = (raw: string): string => {
  const escaped = escapeHtml(raw || '')
  const lines = escaped.split('\n')
  const blocks: string[] = []
  let inCode = false
  let codeLang = ''
  let codeBuffer: string[] = []
  let inUl = false
  let inOl = false

  const closeLists = () => {
    if (inUl) {
      blocks.push('</ul>')
      inUl = false
    }
    if (inOl) {
      blocks.push('</ol>')
      inOl = false
    }
  }

  for (const line of lines) {
    if (line.trimStart().startsWith('```')) {
      if (!inCode) {
        closeLists()
        inCode = true
        codeLang = line.trim().slice(3).trim()
        codeBuffer = []
      } else {
        const cls = codeLang ? ` class="language-${codeLang}"` : ''
        blocks.push(`<pre><code${cls}>${codeBuffer.join('\n')}</code></pre>`)
        inCode = false
        codeLang = ''
        codeBuffer = []
      }
      continue
    }

    if (inCode) {
      codeBuffer.push(line)
      continue
    }

    const ulMatch = /^\s*[-*]\s+(.+)$/.exec(line)
    if (ulMatch) {
      if (!inUl) {
        if (inOl) {
          blocks.push('</ol>')
          inOl = false
        }
        blocks.push('<ul>')
        inUl = true
      }
      blocks.push(`<li>${parseInline(ulMatch[1])}</li>`)
      continue
    }

    const olMatch = /^\s*\d+\.\s+(.+)$/.exec(line)
    if (olMatch) {
      if (!inOl) {
        if (inUl) {
          blocks.push('</ul>')
          inUl = false
        }
        blocks.push('<ol>')
        inOl = true
      }
      blocks.push(`<li>${parseInline(olMatch[1])}</li>`)
      continue
    }

    closeLists()

    const hMatch = /^(#{1,6})\s+(.+)$/.exec(line)
    if (hMatch) {
      const level = hMatch[1].length
      blocks.push(`<h${level}>${parseInline(hMatch[2])}</h${level}>`)
      continue
    }

    if (!line.trim()) {
      blocks.push('<br>')
      continue
    }

    blocks.push(`<p>${parseInline(line)}</p>`)
  }

  if (inCode) {
    const cls = codeLang ? ` class="language-${codeLang}"` : ''
    blocks.push(`<pre><code${cls}>${codeBuffer.join('\n')}</code></pre>`)
  }
  closeLists()

  return blocks.join('')
}

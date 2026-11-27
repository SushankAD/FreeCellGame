import { useCallback, useEffect, useState } from 'react'
import './App.css'

type Card = { rank: number; suit: number; rankName: string; suitName: string; red: boolean; image: string }
type Stack = { number: number; type: 'cell' | 'foundation' | 'cascade'; cards: Card[] }
type Game = { stacks: Stack[]; moveCount: number; gameOver: boolean; canUndo: boolean; elapsedSeconds: number }
type Dialog = 'welcome' | 'rules' | 'stats' | 'win' | null

const SUIT_SYMBOLS = ['♣', '♦', '♥', '♠']

async function api<T>(path: string, options?: RequestInit): Promise<T> {
  const response = await fetch(path, {
    ...options,
    headers: { 'Content-Type': 'application/json', ...options?.headers },
  })
  if (!response.ok) {
    const body = await response.json().catch(() => ({ message: 'The request failed.' }))
    throw new Error(body.message)
  }
  return response.json()
}

function formatCard(card: Card) {
  return `${card.rankName}${SUIT_SYMBOLS[card.suit]}`
}

function formatTime(seconds: number) {
  return `${Math.floor(seconds / 60)}:${String(seconds % 60).padStart(2, '0')}`
}

function formatWinRate(won: number, concluded: number) {
  if (concluded === 0) return '0%'
  return `${Math.round((won * 100) / concluded)}%`
}

function App() {
  const [game, setGame] = useState<Game | null>(null)
  const [selected, setSelected] = useState<number | null>(null)
  const [status, setStatus] = useState('Rank order: K (highest) > Q > J > 10 > 9 > 8 > 7 > 6 > 5 > 4 > 3 > 2 > A (lowest).')
  const [statusType, setStatusType] = useState<'normal' | 'selected' | 'error' | 'success'>('normal')
  const [dialog, setDialog] = useState<Dialog>('welcome')
  const [elapsed, setElapsed] = useState(0)

  // Session stats (mirror JavaFX FreecellWindow behavior)
  const [gamesStarted, setGamesStarted] = useState(1)
  const [gamesWon, setGamesWon] = useState(0)
  const [dealsAbandoned, setDealsAbandoned] = useState(0)
  const [gameCompleted, setGameCompleted] = useState(false)

  useEffect(() => {
    api<Game>('/api/game').then(setGame).catch((event) => {
      setStatus(event.message)
      setStatusType('error')
    })
  }, [])

  useEffect(() => {
    if (!game || game.gameOver) return
    setElapsed(game.elapsedSeconds)
    const timer = window.setInterval(() => setElapsed((value) => value + 1), 1000)
    return () => window.clearInterval(timer)
  }, [game?.gameOver, game?.elapsedSeconds])

  // Check for win after game state updates
  useEffect(() => {
    if (game?.gameOver && !gameCompleted) {
      setGameCompleted(true)
      setGamesWon((prev) => prev + 1)
      setDialog('win')
    }
  }, [game?.gameOver, gameCompleted])

  const newGame = useCallback(async () => {
    // Track abandoned deal if current game wasn't completed
    if (!gameCompleted && game) {
      setDealsAbandoned((prev) => prev + 1)
    }
    try {
      const next = await api<Game>('/api/game/new', { method: 'POST', body: '{}' })
      setGame(next)
      setElapsed(0)
      setSelected(null)
      setStatus('New deal ready.')
      setStatusType('normal')
      setDialog(null)
      setGameCompleted(false)
      setGamesStarted((prev) => prev + 1)
    } catch (event) {
      setStatus(event instanceof Error ? event.message : 'Could not start a new deal.')
      setStatusType('error')
    }
  }, [game, gameCompleted])

  async function chooseStack(number: number) {
    if (!game) return
    if (selected === null) {
      const stack = game.stacks.find((item) => item.number === number)
      if (!stack?.cards.length) {
        setStatus('That stack is empty.')
        setStatusType('normal')
        return
      }
      const topCard = stack.cards[stack.cards.length - 1]
      setSelected(number)
      setStatus(`Selected ${formatCard(topCard)} — click a destination, or click this card again to clear selection.`)
      setStatusType('selected')
      return
    }
    if (selected === number) {
      setSelected(null)
      setStatus('Selection cleared.')
      setStatusType('normal')
      return
    }
    try {
      const next = await api<Game>('/api/game/move', {
        method: 'POST',
        body: JSON.stringify({ source: selected, destination: number }),
      })
      setGame(next)
      setSelected(null)
      setStatus('Move complete.')
      setStatusType('success')
    } catch (event) {
      const message = event instanceof Error ? event.message : 'Move failed.'
      setStatus(`Can't move: ${message}`)
      setStatusType('error')
    }
  }

  async function undo() {
    try {
      const next = await api<Game>('/api/game/undo', { method: 'POST' })
      setGame(next)
      setSelected(null)
      setStatus('Last move undone. Select a card to move, then click its destination.')
      setStatusType('normal')
      // If we undo a winning move, reverse the win tracking
      if (gameCompleted && !next.gameOver) {
        setGameCompleted(false)
        setGamesWon((prev) => Math.max(0, prev - 1))
      }
    } catch (event) {
      setStatus(event instanceof Error ? event.message : 'Could not undo.')
      setStatusType('error')
    }
  }

  function renderStack(stack: Stack) {
    const isSelected = selected === stack.number
    return (
      <button
        className={`stack ${stack.type}`}
        key={stack.number}
        type="button"
        onClick={() => chooseStack(stack.number)}
        aria-label={`${stack.type} ${stack.number}`}
      >
        {stack.cards.length === 0 ? (
          <span className="empty-label">
            {stack.type === 'foundation' ? SUIT_SYMBOLS[stack.number - 5] : ''}
          </span>
        ) : (
          <span className="cards">
            {stack.cards.map((card, index) => {
              const isTopCard = index === stack.cards.length - 1
              return (
                <img
                  key={`${card.rank}-${card.suit}-${index}`}
                  className={`card ${isSelected && isTopCard ? 'selected-card' : ''}`}
                  src={`/cards/${card.image}`}
                  alt={`${card.rankName}${card.suitName}`}
                  style={{ top: stack.type === 'cascade' ? index * 24 : 0 }}
                />
              )
            })}
          </span>
        )}
        <span className="stack-number">{stack.number}</span>
      </button>
    )
  }

  if (!game) return <main className="loading">{status || 'Loading FreeCell...'}</main>

  const cells = game.stacks.filter((stack) => stack.type === 'cell')
  const foundations = game.stacks.filter((stack) => stack.type === 'foundation')
  const cascades = game.stacks.filter((stack) => stack.type === 'cascade')
  const concludedDeals = gamesWon + dealsAbandoned

  return (
    <main className="app">
      <header className="toolbar">
        <div className="brand">
          <strong>FREECELL</strong>
        </div>
        <div className="toolbar-actions">
          <button type="button" className="btn-secondary" onClick={() => setDialog('rules')}>Rules</button>
          <button type="button" className="btn-secondary" onClick={() => setDialog('stats')}>Stats</button>
          <button type="button" className="btn-secondary" onClick={undo} disabled={!game.canUndo}>Undo</button>
          <span className="toolbar-stat">{game.moveCount} MOVES</span>
          <span className="timer">TIME {formatTime(elapsed)}</span>
          <button type="button" className="btn-primary" onClick={newGame}>New deal</button>
        </div>
      </header>

      <section className="board" aria-label="FreeCell board">
        <div className="board-top">
          <div className="stack-group">
            <div className="section-label">Cells</div>
            <div className="stack-row">{cells.map(renderStack)}</div>
          </div>
          <div className="stack-group">
            <div className="section-label">Foundations</div>
            <div className="stack-row">{foundations.map(renderStack)}</div>
          </div>
        </div>
        <div className="cascade-section">
          <div className="section-label cascades-label">Cascades</div>
          <div className="cascades">{cascades.map(renderStack)}</div>
        </div>
      </section>

      <footer className={`status ${statusType}`}>{status}</footer>

      {/* Welcome overlay with full rules */}
      {dialog === 'welcome' && (
        <div className="overlay">
          <section className="dialog welcome-dialog">
            <div className="rules-scroll">
              <RulesPanel />
            </div>
            <div className="dialog-actions">
              <button type="button" className="btn-primary" onClick={() => setDialog(null)}>
                I understand — Start playing
              </button>
            </div>
          </section>
        </div>
      )}

      {/* Rules dialog */}
      {dialog === 'rules' && (
        <div className="overlay">
          <section className="dialog rules-dialog">
            <div className="rules-scroll">
              <RulesPanel />
            </div>
            <div className="dialog-actions">
              <button type="button" className="btn-primary" onClick={() => setDialog(null)}>Done</button>
            </div>
          </section>
        </div>
      )}

      {/* Statistics dialog */}
      {dialog === 'stats' && (
        <div className="overlay">
          <section className="dialog stats-dialog">
            <div className="dialog-header">
              <span className="eyebrow">FREECELL &nbsp;/&nbsp; SESSION STATS</span>
              <h2>Your progress</h2>
              <p className="subtitle">A summary of your games in this session.</p>
            </div>

            <div className="stat-cards">
              <div className="stat-card featured">
                <span className="stat-title">WIN RATE</span>
                <span className="stat-value">{formatWinRate(gamesWon, concludedDeals)}</span>
              </div>
              <div className="stat-card">
                <span className="stat-title">GAMES WON</span>
                <span className="stat-value">{gamesWon}</span>
              </div>
              <div className="stat-card">
                <span className="stat-title">DEALS STARTED</span>
                <span className="stat-value">{gamesStarted}</span>
              </div>
            </div>

            <div className="stat-rows">
              <div className="stat-row">
                <span>Deals abandoned</span>
                <strong>{dealsAbandoned}</strong>
              </div>
              <div className="stat-row">
                <span>Moves in this deal</span>
                <strong>{game.moveCount}</strong>
              </div>
            </div>

            <div className="stat-time-banner">
              Time in this deal: {formatTime(elapsed)}
            </div>

            <div className="dialog-actions">
              <button type="button" className="btn-primary" onClick={() => setDialog(null)}>Done</button>
            </div>
          </section>
        </div>
      )}

      {/* Win dialog */}
      {dialog === 'win' && (
        <div className="overlay">
          <section className="dialog win-dialog">
            <div className="dialog-header">
              <span className="eyebrow">FREECELL &nbsp;/&nbsp; DEAL COMPLETE</span>
              <h2>You won!</h2>
              <p className="subtitle">A summary of your completed deal.</p>
            </div>

            <div className="stat-cards">
              <div className="stat-card featured">
                <span className="stat-title">MOVES</span>
                <span className="stat-value">{game.moveCount}</span>
              </div>
              <div className="stat-card">
                <span className="stat-title">TIME</span>
                <span className="stat-value">{formatTime(elapsed)}</span>
              </div>
            </div>

            <div className="stat-rows">
              <div className="stat-row">
                <span>Cards home</span>
                <strong>52 / 52</strong>
              </div>
              <div className="stat-row">
                <span>Deal status</span>
                <strong>Complete</strong>
              </div>
            </div>

            <div className="stat-time-banner">
              All 52 cards are home. Well played!
            </div>

            <div className="dialog-actions two-buttons">
              <button type="button" className="btn-secondary" onClick={() => setDialog(null)}>
                Keep looking at the board
              </button>
              <button type="button" className="btn-primary" onClick={newGame}>
                Play another deal
              </button>
            </div>
          </section>
        </div>
      )}
    </main>
  )
}

function RulesPanel() {
  return (
    <div className="rules-panel">
      <h2 className="rules-heading">A little strategy goes a long way</h2>
      <p className="rules-intro">The essentials for a smooth deal</p>

      <Rule title="THE GOAL"
        description="Move all 52 cards to the four foundations, building each suit from Ace up to King." />
      <Rule title="RANK ORDER"
        description="King is highest and Ace is lowest: K, Q, J, 10, 9, 8, 7, 6, 5, 4, 3, 2, A." />
      <Rule title="MOVE A CARD"
        description="First click the card you want to move, then click where it should go. To put 2 on 3, select 2 first and then click 3. Click the selected stack again to clear the selection." />
      <Rule title="BUILD THE CASCADES"
        description="Move a card exactly one rank lower onto a card one rank higher, and alternate colors. For example, put a black 7 on a red 8, not an 8 on a 7. Any card can start an empty cascade." />
      <Rule title="USE THE FREE CELLS"
        description="Each of the four free cells holds one card. Use them to make room and uncover cards." />
      <Rule title="BUILD THE FOUNDATIONS"
        description="Start each foundation with its matching Ace, then build upward in the same suit." />
      <Rule title="START PLAYING"
        description={'Your deal starts when you choose "I understand — Start playing". Use Rules any time for a reminder.'} />
    </div>
  )
}

function Rule({ title, description }: { title: string; description: string }) {
  return (
    <div className="rule">
      <div className="rule-title">{title}</div>
      <div className="rule-description">{description}</div>
    </div>
  )
}

export default App

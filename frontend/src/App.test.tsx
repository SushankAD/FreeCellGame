import { cleanup, fireEvent, render, screen } from '@testing-library/react'
import { afterEach, expect, it, vi } from 'vitest'
import App from './App'

afterEach(cleanup)

it('shows the welcome dialog and starts play', async () => {
  vi.stubGlobal('fetch', vi.fn().mockResolvedValue({
    ok: true,
    json: async () => ({
      stacks: Array.from({ length: 16 }, (_, index) => ({
        number: index + 1,
        type: index < 4 ? 'cell' : index < 8 ? 'foundation' : 'cascade',
        cards: [],
      })),
      moveCount: 0,
      gameOver: false,
      canUndo: false,
      elapsedSeconds: 0,
    }),
  }))

  render(<App />)
  // Welcome dialog shows rules heading
  expect(await screen.findByText('A little strategy goes a long way')).toBeTruthy()
  fireEvent.click(screen.getByRole('button', { name: /start playing/i }))
  // After dismissal, the rules heading should be gone
  expect(screen.queryByText('A little strategy goes a long way')).toBeNull()
})

it('highlights only the top card when selecting a stack', async () => {
  vi.stubGlobal('fetch', vi.fn().mockResolvedValue({
    ok: true,
    json: async () => ({
      stacks: Array.from({ length: 16 }, (_, index) => ({
        number: index + 1,
        type: index < 4 ? 'cell' : index < 8 ? 'foundation' : 'cascade',
        cards: index === 8 ? [
          { rank: 5, suit: 0, rankName: '6', suitName: 'C', red: false, image: '6C.png' },
          { rank: 9, suit: 3, rankName: '10', suitName: 'S', red: false, image: '10S.png' },
        ] : [],
      })),
      moveCount: 0,
      gameOver: false,
      canUndo: false,
      elapsedSeconds: 0,
    }),
  }))

  render(<App />)
  fireEvent.click(await screen.findByRole('button', { name: /start playing/i }))
  fireEvent.click(screen.getByRole('button', { name: 'cascade 9' }))

  const bottomCard = screen.getByAltText('6C')
  const topCard = screen.getByAltText('10S')
  expect(bottomCard.className).not.toContain('selected-card')
  expect(topCard.className).toContain('selected-card')
  expect(screen.getByText(/Selected 10♠/)).toBeTruthy()
})

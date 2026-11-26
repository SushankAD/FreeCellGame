import { fireEvent, render, screen } from '@testing-library/react'
import { expect, it, vi } from 'vitest'
import App from './App'

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

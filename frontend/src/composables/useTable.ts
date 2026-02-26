import { ref, reactive } from 'vue'
import type { PageResult } from '@/types'

export interface TableOptions<T> {
  fetchData: (params: { page: number; pageSize: number }) => Promise<PageResult<T>>
  defaultPageSize?: number
}

export function useTable<T>(options: TableOptions<T>) {
  const { fetchData, defaultPageSize = 10 } = options

  const loading = ref(false)
  const tableData = ref<T[]>([]) as any
  const pagination = reactive({
    page: 1,
    pageSize: defaultPageSize,
    total: 0
  })

  const fetchDataList = async () => {
    loading.value = true
    try {
      const result = await fetchData({
        page: pagination.page,
        pageSize: pagination.pageSize
      })
      tableData.value = result.list || []
      pagination.total = result.total || 0
    } catch (error) {
      console.error('Failed to fetch data:', error)
      tableData.value = []
    } finally {
      loading.value = false
    }
  }

  const handlePageChange = (page: number) => {
    pagination.page = page
    fetchDataList()
  }

  const handleSizeChange = (size: number) => {
    pagination.pageSize = size
    pagination.page = 1
    fetchDataList()
  }

  const refresh = () => {
    pagination.page = 1
    fetchDataList()
  }

  return {
    loading,
    tableData,
    pagination,
    fetchDataList,
    handlePageChange,
    handleSizeChange,
    refresh
  }
}
